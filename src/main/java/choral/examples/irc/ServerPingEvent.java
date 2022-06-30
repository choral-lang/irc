package choral.examples.irc;

public class ServerPingEvent extends ServerEvent {
    private PingMessage message;

    public ServerPingEvent(PingMessage message) {
        super(ServerEventType.PING);
        this.message = message;
    }

    public PingMessage getMessage() {
        return message;
    }
}
