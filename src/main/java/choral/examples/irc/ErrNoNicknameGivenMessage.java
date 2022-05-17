package choral.examples.irc;

import java.util.List;

public class ErrNoNicknameGivenMessage extends Message {
    public ErrNoNicknameGivenMessage(String nickname, String message) {
        super(Message.ERR_NONICKNAMEGIVEN, List.of(nickname, message));
    }
}
