package choral.examples.irc;

import java.util.List;

public class PingMessage extends Message {
    public PingMessage(String token) {
        super(null, Command.PING.code(), List.of(token));
    }

    public PingMessage(Message message) {
        super(message);
        assert command == Command.PING.code();
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public String getToken() {
        assert params.size() >= 1;
        return getParam(0);
    }
}
