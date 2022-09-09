package choral.examples.irc;

import java.util.List;

public class PongMessage extends Message {
    public PongMessage(String token) {
        super(null, Command.PONG.string(), List.of(token));
    }

    public PongMessage(String host, String token) {
        super(null, Command.PONG.string(), List.of(host, token));
    }

    public PongMessage(Message message) {
        super(message);
        assert command.equals(Command.PONG.string());
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public String getToken() {
        assert params.size() >= 1;
        return getParam(params.size() == 1 ? 0 : 1);
    }
}
