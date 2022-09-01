package choral.examples.irc;

import java.util.List;

public class QuitMessage extends Message {
    public QuitMessage() {
        super(null, Command.QUIT.string(), List.of());
    }

    public QuitMessage(String reason) {
        super(null, Command.QUIT.string(), List.of(reason));
    }

    public QuitMessage(Message message) {
        super(message);
        assert command == Command.QUIT.string();
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public String getReason() {
        assert params.size() >= 1;
        return getParam(0);
    }
}
