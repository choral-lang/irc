package choral.examples.irc;

import java.util.List;

public class ErrNeedMoreParamsMessage extends Message {
    public ErrNeedMoreParamsMessage(String nickname, String message) {
        super(null, Command.ERR_NEEDMOREPARAMS.code(),
              List.of(nickname, message));
    }

    public ErrNeedMoreParamsMessage(Message message) {
        super(message);
        assert command == Command.ERR_NEEDMOREPARAMS.code();
    }
}
