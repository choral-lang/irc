package choral.examples.irc;

import java.util.List;

public class ErrorMessage extends Message {
    public ErrorMessage(String reason) {
        super(null, Command.ERROR.string(), List.of(reason));
    }

    public ErrorMessage(Message message) {
        super(message);
        assert command.equals(Command.ERROR.string());
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public String getReason() {
        assert params.size() >= 1;
        return getParam(0);
    }
}
