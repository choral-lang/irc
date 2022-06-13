package choral.examples.irc;

public class ClientLocalPongEvent extends ClientLocalEvent {
    private PingMessage message;

    public ClientLocalPongEvent(PingMessage message) {
        super(ClientLocalEventType.PONG);
        this.message = message;
    }

    public PingMessage getMessage() {
        return message;
    }
}
