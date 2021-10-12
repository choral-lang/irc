package choral.examples.irc;

import java.util.List;

public class ClientJoinEvent extends ClientEvent {
    private List<String> channels;

    private ClientJoinEvent() {
        super(ClientEventType.JOIN);
    }

    public ClientJoinEvent(List<String> channels) {
        this();
        this.channels = channels;
    }

    public ClientJoinEvent(String channel) {
        this(List.of(channel));
    }

    public List<String> getChannels() {
        return channels;
    }
}
