package choral.examples.irc;

import java.util.Arrays;
import java.util.List;

public class PartMessage extends Message {
    public PartMessage(List<String> channels, String reason) {
        super(null, Command.PART.string(),
              List.of(String.join(",", channels), reason));
    }

    public PartMessage(List<String> channels) {
        super(null, Command.PART.string(), List.of(String.join(",", channels)));
    }

    public PartMessage(String channel, String reason) {
        this(List.of(channel), reason);
    }

    public PartMessage(String channel) {
        this(List.of(channel));
    }

    public PartMessage(Message message) {
        super(message);
        assert command == Command.PART.string();
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public List<String> getChannels() {
        assert params.size() >= 1;
        return Arrays.asList(getParam(0).split(","));
    }

    public boolean hasReason() {
        return params.size() >= 2;
    }

    public String getReason() {
        assert params.size() >= 2;
        return getParam(1);
    }
}
