package choral.examples.irc;

import java.util.List;

public class IrcClientLocalUtil {
    public static void addMembers(ClientState state, String channel,
                                  List<String> members) {
        for (String member : members) {
            if (!member.equals(state.getNickname())) {
                state.addMember(channel, member);
            }
        }
    }
}
