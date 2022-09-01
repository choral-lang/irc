package choral.examples.irc;

import java.util.List;

public class ExitMessage extends Message {
    private static final String EXIT = "EXIT";

    public ExitMessage() {
        super(null, EXIT, List.of());
    }

    public ExitMessage(Message message) {
        super(message);
        assert command.equals(EXIT);
    }
}
