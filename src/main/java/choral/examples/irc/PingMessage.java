package choral.examples.irc;

import java.util.List;

public class PingMessage extends Message {
    public PingMessage(String token) {
        super(null, Command.PING.string(), List.of(token));
    }

    public PingMessage(Message message) {
        super(message);
        assert command.equals(Command.PING.string());
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public String getToken() {
        assert params.size() >= 1;
        return getParam(0);
    }
}
