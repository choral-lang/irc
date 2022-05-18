package choral.examples.irc;

import java.util.List;

public class ErrNeedMoreParamsMessage extends Message {
    public ErrNeedMoreParamsMessage(String nickname, String message) {
        super(null, Message.ERR_NEEDMOREPARAMS, List.of(nickname, message));
    }
}
