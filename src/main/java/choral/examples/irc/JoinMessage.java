package choral.examples.irc;

import java.util.Arrays;
import java.util.List;

public class JoinMessage extends Message {
    public JoinMessage(List<String> channels) {
        super(null, Command.JOIN.string(), List.of(String.join(",", channels)));
    }

    public JoinMessage(String channel) {
        this(List.of(channel));
    }

    public JoinMessage(Message message) {
        super(message);
        assert command.equals(Command.JOIN.string());
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public List<String> getChannels() {
        assert params.size() >= 1;
        return Arrays.asList(getParam(0).split(","));
    }
}
