package choral.examples.irc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The only purpose of this class is to be a collection of utilities useful for
 * the IrcServerLocal choreography. We put here anything that would be too
 * inconvenient, cumbersome or impossible to write directly in Choral.
 */
public class IrcServerLocalUtil {
    /**
     * Check whether all of the channel names in the given list are valid.
     *
     * NOTE: Choral doesn't support loops or lambda expressions.
     */
    public static boolean validChannelnames(List<String> channels) {
        return channels.stream().allMatch(c -> Util.validChannelname(c));
    }

    /**
     * Process a client's JOIN message.
     *
     * NOTE: Choral doesn't support loops.
     */
    public static void processJoin(ServerState state, long clientId,
                                   JoinMessage message) {
        List<String> channels = message.getChannels();

        if (channels.size() == 1 && channels.get(0) == "0") {
            processPart(state, clientId, new PartMessage(
                new ArrayList<>(state.getChannels(clientId))));
        }
        else {
            String nickname = state.getNickname(clientId);

            for (String channel : new HashSet<>(channels)) {
                if (!Util.validChannelname(channel)) {
                    Message r = new ErrNoSuchChannelMessage(
                        nickname, "Invalid channel name");
                    state.addEvent(clientId, new ServerForwardMessageEvent(r));
                }
                else if (!state.inChannel(clientId, channel)) {
                    JoinMessage r = IrcServerLocalUtil.<JoinMessage>withSource(
                        new JoinMessage(channel),
                        new Source(nickname));

                    Set<Long> members = state.getMembers(channel);

                    state.joinChannel(clientId, channel);
                    state.addEvent(clientId, new ServerJoinEvent(r));
                    state.addEvent(clientId, new ServerRplNamReplyEvent(
                        new RplNamReplyMessage(nickname, "=", channel,
                                               List.of(nickname))));

                    for (Long otherId : members) {
                        state.addEvent(otherId, new ServerJoinEvent(r));

                        RplNamReplyMessage m = new RplNamReplyMessage(
                            nickname, "=", channel,
                            List.of(state.getNickname(otherId)));
                        state.addEvent(clientId, new ServerRplNamReplyEvent(m));
                    }

                    state.addEvent(clientId, new ServerForwardMessageEvent(
                        new RplEndOfNamesMessage(nickname, channel,
                                                 "End of names list")));
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
        List<String> channels = message.getChannels();

        String nickname = state.getNickname(clientId);

        for (String channel : new HashSet<>(channels)) {
            if (!Util.validChannelname(channel)) {
                Message r = new ErrNoSuchChannelMessage(
                    nickname, "Invalid channel name");
                state.addEvent(clientId, new ServerForwardMessageEvent(r));
            }
            else if (!state.inChannel(clientId, channel)) {
                Message r = new ErrNotOnChannelMessage(
                    nickname, "You are not in that channel");
                state.addEvent(clientId, new ServerForwardMessageEvent(r));
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

                PartMessage r = IrcServerLocalUtil.<PartMessage>withSource(
                    new PartMessage(mb.message()),
                    new Source(nickname));

                state.partChannel(clientId, channel);
                state.addEvent(clientId, new ServerPartEvent(r));

                for (Long otherId : state.getMembers(channel)) {
                    state.addEvent(otherId, new ServerPartEvent(r));
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
