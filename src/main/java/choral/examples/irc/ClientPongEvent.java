package choral.examples.irc;

public class ClientPongEvent extends ClientEvent {
    private PongMessage message;

    public ClientPongEvent(PongMessage message) {
        super(ClientEventType.PONG);
        this.message = message;
    }

    public PongMessage getMessage() {
        return message;
    }
}
