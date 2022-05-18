package choral.examples.irc;

import java.util.List;

public class ErrAlreadyRegisteredMessage extends Message {
    public ErrAlreadyRegisteredMessage(String nickname, String message) {
        super(null, Message.ERR_ALREADYREGISTERED, List.of(nickname, message));
    }
}
