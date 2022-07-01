package choral.examples.irc;

import java.util.List;

public class ClientUtil {
    public static void processNick(ClientState state, NickMessage message) {
        Source source = message.getSource();

        if (source == null)
            return;

        String current = state.getNickname();
        String from = source.getNickname();
        String to = message.getNickname();

        if (current != null && current.equals(from)) {
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
