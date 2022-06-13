package choral.examples.irc;

import java.util.List;

public class ErrNotOnChannelMessage extends Message {
    public ErrNotOnChannelMessage(String nickname, String message) {
        super(null, Command.ERR_NOTONCHANNEL.code(),
              List.of(nickname, message));
    }

    public ErrNotOnChannelMessage(Message message) {
        super(message);
        assert command == Command.ERR_NOTONCHANNEL.code();
    }
}
