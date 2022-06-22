package choral.examples.irc;

import java.util.List;

public class ErrAlreadyRegisteredMessage extends Message {
    public ErrAlreadyRegisteredMessage(String nickname, String message) {
        super(null, Command.ERR_ALREADYREGISTERED.code(),
              List.of(nickname, message));
    }

    public ErrAlreadyRegisteredMessage(Message message) {
        super(message);
        assert command == Command.ERR_ALREADYREGISTERED.code();
    }
}
