package choral.examples.irc;

import java.util.List;

public class ServerJoinEvent extends ServerEvent {
    private List<String> channels;

    private ServerJoinEvent() {
        super(ServerEventType.JOIN);
    }

    public ServerJoinEvent(List<String> channels) {
        this();
        this.channels = channels;
    }

    public ServerJoinEvent(String channel) {
        this(List.of(channel));
    }

    public List<String> getChannels() {
        return channels;
    }
}
