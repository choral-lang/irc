package choral.examples.irc;

import java.util.List;

public class PongMessage extends Message {
    public PongMessage(String token) {
        super(null, Message.PONG, List.of(token));
    }

    public PongMessage(String host, String token) {
        super(null, Message.PONG, List.of(host, token));
    }

    public PongMessage(Message message) {
        super(message);
        assert command == Message.PONG;
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public String getToken() {
        assert params.size() >= 1;
        return getParam(params.size() == 1 ? 0 : 1);
    }
}
