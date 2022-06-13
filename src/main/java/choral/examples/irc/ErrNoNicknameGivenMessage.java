package choral.examples.irc;

import java.util.List;

public class ErrNoNicknameGivenMessage extends Message {
    public ErrNoNicknameGivenMessage(String nickname, String message) {
        super(null, Command.ERR_NONICKNAMEGIVEN.code(),
              List.of(nickname, message));
    }
}
