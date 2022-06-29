package choral.examples.irc;

import java.util.List;

public class IrcClientLocalUtil {
    public static void processNick(ClientState state, NickMessage message) {
        Source source = message.getSource();
        String from = source.getNickname();
        String to = message.getNickname();

        if (source == null)
            return;

        if (state.getNickname().equals(from)) {
            state.setNickname(to);
        }
        else {
            state.renameMember(from, to);
        }
    }

    public static void addMembers(ClientState state, String channel,
                                  List<String> members) {
        for (String member : members) {
            if (!member.equals(state.getNickname())) {
                state.addMember(channel, member);
            }
        }
    }
}
