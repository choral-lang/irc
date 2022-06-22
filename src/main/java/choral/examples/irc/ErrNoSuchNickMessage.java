package choral.examples.irc;

import java.util.List;

public class ErrNoSuchNickMessage extends Message {
    public ErrNoSuchNickMessage(String nickname, String message) {
        super(null, Command.ERR_NOSUCHNICK.code(),
              List.of(nickname, message));
    }

    public ErrNoSuchNickMessage(Message message) {
        super(message);
        assert command == Command.ERR_NOSUCHNICK.code();
    }
}
