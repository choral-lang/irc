package choral.examples.irc;

public class ClientPingEvent extends ClientEvent {
    private PingMessage message;

    public ClientPingEvent(PingMessage message) {
        super(ClientEventType.PING);
        this.message = message;
    }

    public PingMessage getMessage() {
        return message;
    }
}
