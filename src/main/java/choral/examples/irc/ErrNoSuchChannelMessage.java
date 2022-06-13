package choral.examples.irc;

import java.util.List;

public class ErrNoSuchChannelMessage extends Message {
    public ErrNoSuchChannelMessage(String nickname, String message) {
        super(null, Command.ERR_NOSUCHCHANNEL.code(),
              List.of(nickname, message));
    }

    public ErrNoSuchChannelMessage(Message message) {
        super(message);
        assert command == Command.ERR_NOSUCHCHANNEL.code();
    }
}
