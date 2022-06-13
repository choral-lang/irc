package choral.examples.irc;

import java.util.List;

public class ErrNotRegisteredMessage extends Message {
    public ErrNotRegisteredMessage(String nickname, String message) {
        super(null, Message.ERR_NOTREGISTERED, List.of(nickname, message));
    }
}
