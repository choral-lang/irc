package choral.examples.irc;

import java.util.ArrayList;
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
public class IrcServerLocalUtil {
    public static void processWelcome(ServerState state, long clientId) {
        BiConsumer<Command, String> add = (command, text) -> {
            Message m = MessageBuilder
                .build()
                .source(new Source("irc.choral.net"))
                .command(IrcServerLocalUtil.commandCode(command))
                .param(state.getNickname(clientId))
                .param(text)
                .message();

            state.addEvent(clientId, new ServerForwardMessageEvent(m));
        };

        state.addEvent(clientId, new ServerRplWelcomeEvent(
            IrcServerLocalUtil.<RplWelcomeMessage>withSource(
                new RplWelcomeMessage(state.getNickname(clientId),
                                      "Welcome to ChoralNet!"),
                new Source("irc.choral.net"))));

        add.accept(Command.RPL_YOURHOST, "Your host is irc.choral.net");
        add.accept(Command.RPL_CREATED, "The server was created at IMADA");
        add.accept(Command.RPL_MYINFO, "I'm running ChoralIRC 0.0.1");
        add.accept(Command.RPL_ISUPPORT, "NICKLEN=32");
        add.accept(Command.RPL_UMODEIS, "+i");

        add.accept(Command.RPL_LUSERCLIENT, "There's only me and you here");
        // add.accept(Command.RPL_LUSEROP, "");
        // add.accept(Command.RPL_LUSERUNKNOWN, "");
        // add.accept(Command.RPL_LUSERCHANNELS, "");
        add.accept(Command.RPL_LUSERME, "I have exactly one user---you");
        // add.accept(Command.RPL_LOCALUSERS, "");
        // add.accept(Command.RPL_GLOBALUSERS, "");

        add.accept(Command.RPL_MOTDSTART, "ChoralNet Message of the Day");
        add.accept(Command.RPL_MOTD, "Hopefully you're having a nice day!");
        add.accept(Command.RPL_MOTD, "Come find us in the office working...");
        add.accept(Command.RPL_MOTD, "...or having a choco break in the lunchroom!");
        add.accept(Command.RPL_ENDOFMOTD, "End of /MOTD command");

        state.setWelcomeDone(clientId, true);
    }

    public static void processNick(ServerState state, long clientId,
                                   NickMessage message) {
        String current = state.getNickname(clientId);

        if (!message.hasEnoughParams()) {
            Message m = new ErrNoNicknameGivenMessage(
                current, "No nickname given");
            state.addEvent(clientId, new ServerForwardMessageEvent(m));
        }
        else {
            String nickname = message.getNickname();

            if (!Util.validNickname(nickname)) {
                Message m = new ErrErroneousNicknameMessage(
                    current, "Nickname is invalid");
                state.addEvent(clientId, new ServerForwardMessageEvent(m));
            }
            else if (state.nicknameExists(nickname)) {
                Message m = new ErrNicknameInUseMessage(
                    current, "Nickname is in use");
                state.addEvent(clientId, new ServerForwardMessageEvent(m));
            }
            else {
                state.setNickname(clientId, nickname);

                if (state.isRegistered(clientId)) {
                    if (!state.isWelcomeDone(clientId)) {
                        processWelcome(state, clientId);
                    }
                    else {
                        NickMessage m = IrcServerLocalUtil.<NickMessage>withSource(
                            message, new Source(current));

                        state.addEvent(clientId, new ServerNickEvent(m));

                        Set<Long> others = state.getChannels(clientId).stream()
                            .flatMap(c -> state.getMembers(c).stream())
                            .collect(Collectors.toSet());

                        for (long otherId : others) {
                            state.addEvent(otherId, new ServerNickEvent(m));
                        }
                    }
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
            Message m = new ErrNotRegisteredMessage(
                "*", "You must register first!");
            state.addEvent(clientId, new ServerForwardMessageEvent(m));
        }
        else if (!message.hasEnoughParams()) {
            Message m = new ErrNeedMoreParamsMessage(
                state.getNickname(clientId), "Need at least 1 parameter!");
            state.addEvent(clientId, new ServerForwardMessageEvent(m));
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
                        Message m = new ErrNoSuchChannelMessage(
                            nickname, "Invalid channel name");
                        state.addEvent(clientId, new ServerForwardMessageEvent(m));
                    }
                    else if (!state.inChannel(clientId, channel)) {
                        JoinMessage m1 = IrcServerLocalUtil.<JoinMessage>withSource(
                            new JoinMessage(channel),
                            new Source(nickname));

                        Set<Long> members = state.getMembers(channel);

                        state.joinChannel(clientId, channel);
                        state.addEvent(clientId, new ServerJoinEvent(m1));
                        state.addEvent(clientId, new ServerRplNamReplyEvent(
                            new RplNamReplyMessage(nickname, "=", channel,
                                                List.of(nickname))));

                        for (Long otherId : members) {
                            state.addEvent(otherId, new ServerJoinEvent(m1));

                            RplNamReplyMessage m2 = new RplNamReplyMessage(
                                nickname, "=", channel,
                                List.of(state.getNickname(otherId)));
                            state.addEvent(clientId, new ServerRplNamReplyEvent(m2));
                        }

                        Message m3 = new RplEndOfNamesMessage(nickname, channel,
                            "End of names list");
                        state.addEvent(clientId, new ServerForwardMessageEvent(m3));
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
            Message m = new ErrNotRegisteredMessage(
                "*", "You must register first!");
            state.addEvent(clientId, new ServerForwardMessageEvent(m));
        }
        else if (!message.hasEnoughParams()) {
            Message m = new ErrNeedMoreParamsMessage(
                state.getNickname(clientId), "Need at least 1 parameter!");
            state.addEvent(clientId, new ServerForwardMessageEvent(m));
        }
        else {
            List<String> channels = message.getChannels();
            String nickname = state.getNickname(clientId);

            for (String channel : new HashSet<>(channels)) {
                if (!Util.validChannelname(channel)) {
                    Message m = new ErrNoSuchChannelMessage(
                        nickname, "Invalid channel name");
                    state.addEvent(clientId, new ServerForwardMessageEvent(m));
                }
                else if (!state.inChannel(clientId, channel)) {
                    Message m = new ErrNotOnChannelMessage(
                        nickname, "You are not in that channel");
                    state.addEvent(clientId, new ServerForwardMessageEvent(m));
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

                    PartMessage m = IrcServerLocalUtil.<PartMessage>withSource(
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

    public static void processPrivmsg(ServerState state, long clientId,
                                      PrivmsgMessage message) {
        if (!state.isRegistered(clientId)) {
            Message m = new ErrNotRegisteredMessage(
                "*", "You must register first!");
            state.addEvent(clientId, new ServerForwardMessageEvent(m));
        }
        else if (!message.hasTargets()) {
            Message m = new ErrNoRecipientMessage(
                state.getNickname(clientId), "No recipient");
            state.addEvent(clientId, new ServerForwardMessageEvent(m));
        }
        else if (!message.hasText()) {
            Message m = new ErrNoTextToSendMessage(
                state.getNickname(clientId), "No text to send");
            state.addEvent(clientId, new ServerForwardMessageEvent(m));
        }
        else {
            String nickname = state.getNickname(clientId);
            String text = message.getText();

            for (String target : message.getTargets()) {
                if (Util.validChannelname(target)) {
                    if (!state.channelExists(target)) {
                        Message m = new ErrNoSuchNickMessage(
                            nickname, "No such channel");
                        state.addEvent(clientId, new ServerForwardMessageEvent(m));
                    }
                    else if (!state.inChannel(clientId, target)) {
                        Message m = new ErrCannotSendToChanMessage(
                            nickname, "Cannot send to channel");
                        state.addEvent(clientId, new ServerForwardMessageEvent(m));
                    }
                    else {
                        PrivmsgMessage m = IrcServerLocalUtil.<PrivmsgMessage>withSource(
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
                        Message m = new ErrNoSuchNickMessage(
                            nickname, "No such nickname");
                        state.addEvent(clientId, new ServerForwardMessageEvent(m));
                    }
                    else {
                        PrivmsgMessage m = IrcServerLocalUtil.<PrivmsgMessage>withSource(
                            new PrivmsgMessage(target, text),
                            new Source(nickname));

                        state.addEvent(state.getClientId(target), new ServerPrivmsgEvent(m));
                    }
                }
            }
        }
    }

    /**
     * Equivalent to command.code().
     *
     * NOTE: Choral's enums do not support methods, so we cannot expose any of
     * <code>Command</code>'s methods through a Choral header file.
     */
    public static String commandCode(Command command) {
        return command.code();
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
}
