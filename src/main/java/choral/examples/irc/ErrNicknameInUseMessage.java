package choral.examples.irc;

import java.util.List;

public class ErrNicknameInUseMessage extends Message {
    public ErrNicknameInUseMessage(String nickname, String message) {
        super(null, Command.ERR_NICKNAMEINUSE.code(),
              List.of(nickname, message));
    }

    public ErrNicknameInUseMessage(Message message) {
        super(message);
        assert command == Command.ERR_NICKNAMEINUSE.code();
    }
}
