package choral.examples.irc;

import java.util.List;

public class ErrNoRecipientMessage extends Message {
    public ErrNoRecipientMessage(String nickname, String message) {
        super(null, Command.ERR_NORECIPIENT.code(),
              List.of(nickname, message));
    }

    public ErrNoRecipientMessage(Message message) {
        super(message);
        assert command == Command.ERR_NORECIPIENT.code();
    }
}
