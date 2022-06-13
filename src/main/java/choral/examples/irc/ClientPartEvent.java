package choral.examples.irc;

import java.util.List;

public class ClientPartEvent extends ClientEvent {
    private List<String> channels;
    private String reason;

    private ClientPartEvent() {
        super(ClientEventType.PART);
    }

    public ClientPartEvent(List<String> channels, String reason) {
        this();
        this.channels = channels;
        this.reason = reason;
    }

    public ClientPartEvent(List<String> channels) {
        this(channels, null);
    }

    public ClientPartEvent(String channel, String reason) {
        this(List.of(channel), reason);
    }

    public ClientPartEvent(String channel) {
        this(channel, null);
    }

    public List<String> getChannels() {
        return channels;
    }

    public String getReason() {
        return reason;
    }
}
