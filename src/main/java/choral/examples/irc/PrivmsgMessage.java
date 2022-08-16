package choral.examples.irc;

import java.util.Arrays;
import java.util.List;

public class PrivmsgMessage extends Message {
    public PrivmsgMessage(List<String> targets, String text) {
        super(null, Command.PRIVMSG.string(),
              List.of(String.join(",", targets), text));
    }

    public PrivmsgMessage(String target, String text) {
        this(List.of(target), text);
    }

    public PrivmsgMessage(Message message) {
        super(message);
        assert command == Command.PRIVMSG.string();
    }

    public boolean hasEnoughParams() {
        return params.size() >= 2;
    }

    public boolean hasTargets() {
        return params.size() >= 1;
    }

    public List<String> getTargets() {
        assert params.size() >= 1;
        return Arrays.asList(getParam(0).split(","));
    }

    public boolean hasText() {
        return params.size() >= 2;
    }

    public String getText() {
        assert params.size() >= 2;
        return getParam(1);
    }
}
