package choral.examples.irc;

import java.util.List;

public class ErrCannotSendToChanMessage extends Message {
    public ErrCannotSendToChanMessage(String nickname, String message) {
        super(null, Command.ERR_CANNOTSENDTOCHAN.code(),
              List.of(nickname, message));
    }

    public ErrCannotSendToChanMessage(Message message) {
        super(message);
        assert command == Command.ERR_CANNOTSENDTOCHAN.code();
    }
}
