package choral.examples.irc;

import java.util.List;

public class ErrNicknameInUseMessage extends Message {
    public ErrNicknameInUseMessage(String nickname, String message) {
        super(null, Message.ERR_NICKNAMEINUSE, List.of(nickname, message));
    }
}
