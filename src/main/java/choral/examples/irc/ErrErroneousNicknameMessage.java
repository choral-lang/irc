package choral.examples.irc;

import java.util.List;

public class ErrErroneousNicknameMessage extends Message {
    public ErrErroneousNicknameMessage(String nickname, String message) {
        super(null, Command.ERR_ERRONEOUSNICKNAME.code(),
              List.of(nickname, message));
    }

    public ErrErroneousNicknameMessage(Message message) {
        super(message);
        assert command == Command.ERR_ERRONEOUSNICKNAME.code();
    }
}
