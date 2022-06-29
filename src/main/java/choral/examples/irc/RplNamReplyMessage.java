package choral.examples.irc;

import java.util.Arrays;
import java.util.List;

public class RplNamReplyMessage extends Message {
    public RplNamReplyMessage(String nickname, String status, String channel,
                              List<String> nicknames) {
        super(null, Command.RPL_NAMREPLY.code(),
              List.of(nickname, status, channel, String.join(" ", nicknames)));
    }

    public RplNamReplyMessage(Message message) {
        super(message);
        assert command == Command.RPL_NAMREPLY.code();
    }

    public boolean hasEnoughParams() {
        return params.size() >= 4;
    }

    public String getNickname() {
        assert params.size() >= 1;
        return params.get(0);
    }

    public String getStatus() {
        assert params.size() >= 2;
        return params.get(1);
    }

    public String getChannel() {
        assert params.size() >= 3;
        return params.get(2);
    }

    public List<String> getNicknames() {
        assert params.size() >= 4;
        return Arrays.asList(params.get(3).split(" "));
    }
}
