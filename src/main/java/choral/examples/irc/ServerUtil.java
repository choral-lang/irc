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
 * the IrcServerLocal choreography. We put here anything that would be too
 * inconvenient, cumbersome or impossible to write directly in Choral.
 */
public class ServerUtil {
    public static final String HOSTNAME = "irc.choral.net";

    /**
     * Process a client's NICK message.
     *
     * NOTE: Choral doesn't support loops.
     */
    public static void processWelcome(ServerState state, long clientId) {
        BiConsumer<Command, String> add = (command, text) -> {
            state.addEvent(clientId, forwardNumeric(
                command, state.getNickname(clientId), text));
        };

        state.addEvent(clientId, new ServerRplWelcomeEvent(
            ServerUtil.withSource(
                new RplWelcomeMessage(state.getNickname(clientId),
                                      "Welcome to ChoralNet!"),
                new Source(HOSTNAME))));

        add.accept(Command.RPL_YOURHOST, "Your host is " + HOSTNAME + "!");
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
     * NOTE: Choral doesn't support loops.
     */
    public static void processNick(ServerState state, long clientId,
                                   NickMessage message) {
        String current = state.getNickname(clientId);

        if (!message.hasEnoughParams()) {
            state.addEvent(clientId, forwardNumeric(
                Command.ERR_NONICKNAMEGIVEN, current, "No nickname given"));
            }
        else {
            String nickname = message.getNickname();

            if (!Util.validNickname(nickname)) {
                state.addEvent(clientId, forwardNumeric(
                    Command.ERR_ERRONEOUSNICKNAME, current,
                    "Nickname is invalid"));
            }
            else if (state.nicknameExists(nickname)) {
                state.addEvent(clientId, forwardNumeric(
                    Command.ERR_NICKNAMEINUSE, current, "Nickname is in use"));
            }
            else {
                state.setNickname(clientId, nickname);

                if (state.isRegistered(clientId)) {
                    NickMessage m = ServerUtil.withSource(
                        message, new Source(current));

                    state.addEvent(clientId, new ServerNickEvent(m));

                    Set<Long> others = state.getChannels(clientId).stream()
                        .flatMap(c -> state.getMembers(c).stream())
                        .collect(Collectors.toSet());
                    others.remove(clientId);

                    for (long otherId : others) {
                        state.addEvent(otherId, new ServerNickEvent(m));
                    }
                }
                else if (state.canRegister(clientId)) {
                    processWelcome(state, clientId);
                }
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
            state.addEvent(clientId, forwardNumeric(
                Command.ERR_NOTREGISTERED, "*", "You must register first"));
        }
        else if (!message.hasEnoughParams()) {
            state.addEvent(clientId, forwardNumeric(
                Command.ERR_NEEDMOREPARAMS, state.getNickname(clientId),
                "Need more parameters"));
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
                        state.addEvent(clientId, forwardNumeric(
                            Command.ERR_NOSUCHCHANNEL, nickname,
                            "Invalid channel name"));
                    }
                    else if (!state.inChannel(clientId, channel)) {
                        JoinMessage m = ServerUtil.withSource(
                            new JoinMessage(channel),
                            new Source(nickname));

                        Set<Long> members = state.getMembers(channel);

                        state.joinChannel(clientId, channel);
                        state.addEvent(clientId, new ServerJoinEvent(m));

                        state.addEvent(clientId, new ServerRplNamReplyEvent(
                            ServerUtil.withSource(
                                new RplNamReplyMessage(
                                    nickname, "=", channel, List.of(nickname)),
                                new Source(HOSTNAME))));

                        for (Long otherId : members) {
                            state.addEvent(otherId, new ServerJoinEvent(m));

                            state.addEvent(clientId, new ServerRplNamReplyEvent(
                                ServerUtil.withSource(
                                    new RplNamReplyMessage(
                                        nickname, "=", channel,
                                        List.of(state.getNickname(otherId))),
                                    new Source(HOSTNAME))));
                        }

                        state.addEvent(clientId, forwardNumeric(
                            Command.RPL_ENDOFNAMES, nickname, channel,
                            "End of names list"));
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
            state.addEvent(clientId, forwardNumeric(
                Command.ERR_NOTREGISTERED, "*", "You must register first"));
        }
        else if (!message.hasEnoughParams()) {
            state.addEvent(clientId, forwardNumeric(
                Command.ERR_NEEDMOREPARAMS, state.getNickname(clientId),
                "Need more parameters"));
        }
        else {
            List<String> channels = message.getChannels();
            String nickname = state.getNickname(clientId);

            for (String channel : new HashSet<>(channels)) {
                if (!Util.validChannelname(channel)) {
                    state.addEvent(clientId, forwardNumeric(
                        Command.ERR_NOSUCHCHANNEL, nickname,
                        "Invalid channel name"));
                }
                else if (!state.inChannel(clientId, channel)) {
                    state.addEvent(clientId, forwardNumeric(
                        Command.ERR_NOTONCHANNEL, nickname,
                        "You are not in that channel"));
                }
                else {
                    MessageBuilder mb = MessageBuilder
                        .build()
                        .command(Command.PART.code())
                        .source(new Source(nickname))
                        .param(channel);

                    if (message.hasReason()) {
                        mb.param(message.getReason());
                    }

                    PartMessage m = ServerUtil.withSource(
                        new PartMessage(mb.message()),
                        new Source(nickname));

                    state.partChannel(clientId, channel);
                    state.addEvent(clientId, new ServerPartEvent(m));

                    for (Long otherId : state.getMembers(channel)) {
                        state.addEvent(otherId, new ServerPartEvent(m));
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
            state.addEvent(clientId, forwardNumeric(
                Command.ERR_NOTREGISTERED, "*", "You must register first"));
        }
        else if (!message.hasTargets()) {
            state.addEvent(clientId, forwardNumeric(
                Command.ERR_NORECIPIENT, state.getNickname(clientId),
                "No recipient"));
        }
        else if (!message.hasText()) {
            state.addEvent(clientId, forwardNumeric(
                Command.ERR_NOTEXTTOSEND, state.getNickname(clientId),
                "No text to send"));
        }
        else {
            String nickname = state.getNickname(clientId);
            String text = message.getText();

            for (String target : message.getTargets()) {
                if (Util.validChannelname(target)) {
                    if (!state.channelExists(target)) {
                        state.addEvent(clientId, forwardNumeric(
                            Command.ERR_NOSUCHNICK, nickname,
                            "No such channel"));
                    }
                    else if (!state.inChannel(clientId, target)) {
                        state.addEvent(clientId, forwardNumeric(
                            Command.ERR_CANNOTSENDTOCHAN, nickname,
                            "You are not in that channel"));
                    }
                    else {
                        PrivmsgMessage m = ServerUtil.withSource(
                            new PrivmsgMessage(target, text),
                            new Source(nickname));

                        Set<Long> others = state.getMembers(target);
                        others.remove(clientId);

                        for (long otherId : others) {
                            state.addEvent(otherId, new ServerPrivmsgEvent(m));
                        }
                    }
                }
                else {
                    if (!state.nicknameExists(target)) {
                        state.addEvent(clientId, forwardNumeric(
                            Command.ERR_NOSUCHNICK, nickname,
                            "No such nickname"));
                    }
                    else {
                        PrivmsgMessage m = ServerUtil.withSource(
                            new PrivmsgMessage(target, text),
                            new Source(nickname));

                        state.addEvent(state.getClientId(target),
                                       new ServerPrivmsgEvent(m));
                    }
                }
            }
        }
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

    public static Message makeNumeric(Command command,
                                      String nickname,
                                      String... params) {
        List<String> ps = new ArrayList<>();
        ps.add(nickname);
        ps.addAll(Arrays.asList(params));

        return MessageBuilder
            .build()
            .source(new Source(HOSTNAME))
            .command(command.code())
            .params(ps)
            .message();
    }

    public static ServerForwardEvent forwardNumeric(Command command,
                                                    String nickname,
                                                    String... params) {
        Message m = makeNumeric(command, nickname, params);
        return new ServerForwardEvent(new ForwardMessage(m));
    }

    public static ServerForwardEvent forwardNumeric(Command command,
                                                    String nickname,
                                                    String param1) {
        return forwardNumeric(command, nickname, new String[] {param1});
    }
}
