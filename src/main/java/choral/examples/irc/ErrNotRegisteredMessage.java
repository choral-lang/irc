package choral.examples.irc;

import java.util.List;

public class ErrNotRegisteredMessage extends Message {
    public ErrNotRegisteredMessage(String nickname, String message) {
        super(null, Command.ERR_NOTREGISTERED.code(),
              List.of(nickname, message));
    }

    public ErrNotRegisteredMessage(Message message) {
        super(message);
        assert command == Command.ERR_NOTREGISTERED.code();
    }
}
