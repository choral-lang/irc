package choral.examples.irc;

import java.util.List;

public class ErrNoTextToSendMessage extends Message {
    public ErrNoTextToSendMessage(String nickname, String message) {
        super(null, Command.ERR_NOTEXTTOSEND.code(),
              List.of(nickname, message));
    }

    public ErrNoTextToSendMessage(Message message) {
        super(message);
        assert command == Command.ERR_NOTEXTTOSEND.code();
    }
}
