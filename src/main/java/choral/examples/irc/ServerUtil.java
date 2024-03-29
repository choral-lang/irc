package choral.examples.irc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * The only purpose of this class is to be a collection of utilities useful for
 * the Server within the Irc choreography. We put here anything that would be
 * too inconvenient, cumbersome or impossible to write directly in Choral.
 */
public class ServerUtil {
    public static void printRecv(ServerState state, long clientId,
                                 Message message) {
        if (state.isDebug()) {
            System.out.println("recv: client " + clientId + ": " +
                               message.toString());
        }
    }

    public static void printSend(ServerState state, long clientId,
                                 Message message) {
        if (state.isDebug()) {
            System.out.println("send: client " + clientId + ": " +
                               message.toString());
        }
    }

    /**
     * Process a client's NICK message.
     *
     * NOTE: Choral doesn't support loops and lambdas.
     */
    public static void processWelcome(ServerState state, long clientId) {
        BiConsumer<Command, String> add = (command, text) -> {
            state.enqueue(clientId, forwardNumeric(
                state, command, state.getNickname(clientId), text));
        };

        state.enqueue(clientId, ServerUtil.withSource(
            new RplWelcomeMessage(state.getNickname(clientId),
                                  "Welcome to ChoralNet!"),
            new Source(state.getHostname())));

        add.accept(Command.RPL_YOURHOST, "Your host is " + state.getHostname() + "!");
        add.accept(Command.RPL_CREATED, "The server was created at IMADA!");
        add.accept(Command.RPL_MYINFO, "I'm running ChoralIRC 0.0.1!");
        add.accept(Command.RPL_ISUPPORT, "NICKLEN=32");
        add.accept(Command.RPL_UMODEIS, "+i");

        add.accept(Command.RPL_LUSERCLIENT, "There's only me and you here!");
        // add.accept(Command.RPL_LUSEROP, "");
        // add.accept(Command.RPL_LUSERUNKNOWN, "");
        // add.accept(Command.RPL_LUSERCHANNELS, "");
        add.accept(Command.RPL_LUSERME, "I have exactly one user---you!");
        // add.accept(Command.RPL_LOCALUSERS, "");
        // add.accept(Command.RPL_GLOBALUSERS, "");

        add.accept(Command.RPL_MOTDSTART, "ChoralNet Message of the Day");
        add.accept(Command.RPL_MOTD, "Hopefully you're having a nice day!");
        add.accept(Command.RPL_MOTD, "Come find us in the office working...");
        add.accept(Command.RPL_MOTD, "...or having a choco break in the lunchroom!");
        add.accept(Command.RPL_ENDOFMOTD, "End of /MOTD command");

        state.setRegistered(clientId);
    }

    /**
     * Process a client's NICK message.
     *
     * NOTE: Choral doesn't support loops and lambdas.
     */
    public static void processNick(ServerState state, long clientId,
                                   NickMessage message) {
        String current = state.getNickname(clientId);

        if (current == null) {
            current = "*";
        }

        if (!message.hasEnoughParams()) {
            state.enqueue(clientId, forwardNumeric(
                state, Command.ERR_NONICKNAMEGIVEN, current,
                "No nickname given"));
        }
        else {
            String nickname = message.getNickname();

            if (!Util.validNickname(nickname)) {
                state.enqueue(clientId, forwardNumeric(
                    state, Command.ERR_ERRONEOUSNICKNAME, current,
                    Message.escapeParam(nickname), "Nickname is invalid"));
            }
            // Try to atomically acquire the nickname
            else if (!state.setNickname(clientId, nickname)) {
                state.enqueue(clientId, forwardNumeric(
                    state, Command.ERR_NICKNAMEINUSE, current, nickname,
                    "Nickname is in use"));
            }
            // Client is changing their nickname
            else if (state.isRegistered(clientId)) {
                NickMessage m = ServerUtil.withSource(
                    message, new Source(current));

                state.enqueue(clientId, m);

                // Synchronize to disallow others joining or leaving the channel
                // (for whatever reason) while we queue up messages
                synchronized (state) {
                    Set<Long> others = state.getChannels(clientId).stream()
                        .flatMap(c -> state.getMembers(c).stream())
                        .collect(Collectors.toSet());
                    others.remove(clientId);

                    for (long otherId : others) {
                        state.enqueue(otherId, m);
                    }
                }
            // Client has only just registered
            } else if (state.canRegister(clientId)) {
                processWelcome(state, clientId);
            }
        }
    }

    /**
     * Process a client's JOIN message.
     *
     * NOTE: Choral doesn't support loops.
     */
    public static void processJoin(ServerState state, long clientId,
                                   JoinMessage message) {
        if (!state.isRegistered(clientId)) {
            state.enqueue(clientId, forwardNumeric(
                state, Command.ERR_NOTREGISTERED, "*",
                "You must register first"));
        }
        else if (!message.hasEnoughParams()) {
            state.enqueue(clientId, forwardNumeric(
                state, Command.ERR_NEEDMOREPARAMS, state.getNickname(clientId),
                Command.JOIN.string(), "Need more parameters"));
        }
        else {
            List<String> channels = message.getChannels();

            if (channels.size() == 1 && channels.get(0).equals("0")) {
                processPart(state, clientId, new PartMessage(
                    new ArrayList<>(state.getChannels(clientId))));
            }
            else {
                String nickname = state.getNickname(clientId);

                for (String channel : new HashSet<>(channels)) {
                    if (!Util.validChannelname(channel)) {
                        state.enqueue(clientId, forwardNumeric(
                            state, Command.ERR_NOSUCHCHANNEL, nickname,
                            Message.escapeParam(channel),
                            "Invalid channel name"));
                    }
                    else if (!state.inChannel(clientId, channel)) {
                        JoinMessage m = ServerUtil.withSource(
                            new JoinMessage(channel),
                            new Source(nickname));

                        state.enqueue(clientId, m);
                        state.enqueue(clientId, ServerUtil.withSource(
                            new RplNamReplyMessage(
                                nickname, "=", channel, List.of(nickname)),
                            new Source(state.getHostname())));

                        // Synchronize to disallow others joining or leaving the
                        // channel (for whatever reason) while we queue up
                        // messages
                        synchronized (state) {
                            Set<Long> members = state.getMembers(channel);
                            Set<String> nicknames = members.stream()
                                .map(otherId -> state.getNickname(otherId))
                                .collect(Collectors.toSet());

                            for (String otherNickname : nicknames) {
                                state.enqueue(clientId, ServerUtil.withSource(
                                    new RplNamReplyMessage(
                                        nickname, "=", channel,
                                        List.of(otherNickname)),
                                    new Source(state.getHostname())));
                            }

                            state.enqueue(clientId, forwardNumeric(
                                state, Command.RPL_ENDOFNAMES, nickname,
                                channel, "End of names list"));

                            state.joinChannel(clientId, channel);

                            for (Long otherId : members) {
                                state.enqueue(otherId, m);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Process a client's PART message.
     *
     * NOTE: Choral doesn't support loops.
     */
    public static void processPart(ServerState state, long clientId,
                                   PartMessage message) {
        if (!state.isRegistered(clientId)) {
            state.enqueue(clientId, forwardNumeric(
                state, Command.ERR_NOTREGISTERED, "*",
                "You must register first"));
        }
        else if (!message.hasEnoughParams()) {
            state.enqueue(clientId, forwardNumeric(
                state, Command.ERR_NEEDMOREPARAMS, state.getNickname(clientId),
                Command.PART.string(), "Need more parameters"));
        }
        else {
            List<String> channels = message.getChannels();
            String nickname = state.getNickname(clientId);

            for (String channel : new HashSet<>(channels)) {
                if (!Util.validChannelname(channel)) {
                    state.enqueue(clientId, forwardNumeric(
                        state, Command.ERR_NOSUCHCHANNEL, nickname,
                        Message.escapeParam(channel), "Invalid channel name"));
                }
                else if (!state.inChannel(clientId, channel)) {
                    state.enqueue(clientId, forwardNumeric(
                        state, Command.ERR_NOTONCHANNEL, nickname, channel,
                        "You are not in that channel"));
                }
                else {
                    MessageBuilder mb = MessageBuilder
                        .build()
                        .command(Command.PART.string())
                        .source(new Source(nickname))
                        .param(channel);

                    if (message.hasReason()) {
                        mb.param(message.getReason());
                    }

                    PartMessage m = ServerUtil.withSource(
                        new PartMessage(mb.message()),
                        new Source(nickname));

                    // Synchronize to disallow others joining or leaving the
                    // channel (for whatever reason) while we queue up messages
                    synchronized (state) {
                        state.partChannel(clientId, channel);
                        state.enqueue(clientId, m);

                        for (Long otherId : state.getMembers(channel)) {
                            state.enqueue(otherId, m);
                        }
                    }
                }
            }
        }
    }

    /**
     * Process a client's PRIVMSG message.
     *
     * NOTE: Choral doesn't support loops.
     */
    public static void processPrivmsg(ServerState state, long clientId,
                                      PrivmsgMessage message) {
        if (!state.isRegistered(clientId)) {
            state.enqueue(clientId, forwardNumeric(
                state, Command.ERR_NOTREGISTERED, "*",
                "You must register first"));
        }
        else if (!message.hasTargets()) {
            state.enqueue(clientId, forwardNumeric(
                state, Command.ERR_NORECIPIENT, state.getNickname(clientId),
                "No recipient"));
        }
        else if (!message.hasText()) {
            state.enqueue(clientId, forwardNumeric(
                state, Command.ERR_NOTEXTTOSEND, state.getNickname(clientId),
                "No text to send"));
        }
        else {
            String nickname = state.getNickname(clientId);
            String text = message.getText();

            for (String target : message.getTargets()) {
                if (Util.validChannelname(target)) {
                    if (!state.inChannel(clientId, target)) {
                        state.enqueue(clientId, forwardNumeric(
                            state, Command.ERR_CANNOTSENDTOCHAN, nickname,
                            target, "You are not in that channel"));
                    }
                    else {
                        PrivmsgMessage m = ServerUtil.withSource(
                            new PrivmsgMessage(target, text),
                            new Source(nickname));

                        // Synchronize to disallow others joining or leaving the
                        // channel (for whatever reason) while we queue up
                        // messages
                        synchronized (state) {
                            Set<Long> others = state.getMembers(target);
                            others.remove(clientId);

                            for (long otherId : others) {
                                state.enqueue(otherId, m);
                            }
                        }
                    }
                }
                else {
                    // Synchronize to disallow someone disappearing while we
                    // send them a message
                    synchronized (state) {
                        if (!state.nicknameExists(target)) {
                            state.enqueue(clientId, forwardNumeric(
                                state, Command.ERR_NOSUCHNICK, nickname, target,
                                "No such nickname"));
                        }
                        else {
                            PrivmsgMessage m = ServerUtil.withSource(
                                new PrivmsgMessage(target, text),
                                new Source(nickname));

                            state.enqueue(state.getClientId(target), m);
                        }
                    }
                }
            }
        }
    }

    /**
     * Send QUIT messages to all clients that share a channel with the given
     * one.
     *
     * NOTE: Choral doesn't support loops and lambdas.
     */
    public static void sendQuits(ServerState state, long clientId,
                                 QuitMessage message) {
        String reason = message.hasEnoughParams() ? message.getReason() : "";
        reason = "Quit: " + reason;

        if (state.isRegistered(clientId)) {
            // Synchronize to disallow others joining or leaving the channel
            // (for whatever reason) while we queue up messages
            synchronized (state) {
                Set<Long> others = state.getChannels(clientId).stream()
                    .flatMap(c -> state.getMembers(c).stream())
                    .collect(Collectors.toSet());
                others.remove(clientId);

                for (long otherId : others) {
                    state.enqueue(otherId, ServerUtil.withSource(
                        new QuitMessage(reason),
                        new Source(state.getNickname(clientId))));
                }
            }
        }
    }

    /**
     * Process a client's QUIT message.
     *
     * NOTE: Choral doesn't support loops.
     */
    public static void processQuit(ServerState state, long clientId,
                                   QuitMessage message) {
        state.enqueue(clientId, ServerUtil.withSource(
            new ErrorMessage("Client quit"),
            new Source(state.getHostname())));

        ServerUtil.sendQuits(state, clientId, message);

        state.quit(clientId);
    }

    /**
     * Return a new instance of <code>Message</code> (or one of its subclasses)
     * that has the same information as <code>message</code>, but with the
     * source set to <code>source</code>.
     *
     * Since <code>Message</code> is immutable, the only way to change the
     * source is to construct a completely new message. This requires us to call
     * <code>Message.construct</code> afterwards in order to construct an
     * instance of the appropriate subclass. The returned instance is then
     * unconditionally cast to the type parameter.
     *
     * For this reason, <code>Message.construct</code> has to return an instance
     * that can be cast to the provided type parameter T.
     */
    public static <T extends Message> T withSource(T message, Source source) {
        @SuppressWarnings("unchecked")
        T m = (T) Message.construct(MessageBuilder
            .build()
            .fromMessage(message)
            .source(source)
            .message());
        return m;
    }

    public static ForwardMessage forwardNumeric(ServerState state,
                                                Command command,
                                                String nickname,
                                                String... params) {
        List<String> ps = new ArrayList<>();
        ps.add(nickname);
        ps.addAll(Arrays.asList(params));

        return new ForwardMessage(
            MessageBuilder
                .build()
                .source(new Source(state.getHostname()))
                .command(command.string())
                .params(ps)
                .message());
    }

    public static ForwardMessage forwardNumeric(ServerState state,
                                                Command command,
                                                String nickname,
                                                String param1) {
        return forwardNumeric(state, command, nickname, new String[] {param1});
    }

    public static ForwardMessage forwardNumeric(ServerState state,
                                                Command command,
                                                String nickname,
                                                String param1,
                                                String param2) {
        return forwardNumeric(state, command, nickname, new String[] {param1, param2});
    }

    public static ForwardMessage forwardNumeric(ServerState state,
                                                Command command,
                                                String nickname,
                                                String param1,
                                                String param2,
                                                String param3) {
        return forwardNumeric(state, command, nickname, new String[] {param1, param2, param3});
    }
}
