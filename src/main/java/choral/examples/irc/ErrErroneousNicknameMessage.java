package choral.examples.irc;

import java.util.List;

public class ErrErroneousNicknameMessage extends Message {
    public ErrErroneousNicknameMessage(String nickname, String message) {
        super(Message.ERR_ERRONEOUSNICKNAME, List.of(nickname, message));
    }
}
