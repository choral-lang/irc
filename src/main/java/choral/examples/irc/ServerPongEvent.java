package choral.examples.irc;

public class ServerPongEvent extends ServerEvent {
    private PongMessage message;

    public ServerPongEvent(PongMessage message) {
        super(ServerEventType.PONG);
        this.message = message;
    }

    public PongMessage getMessage() {
        return message;
    }
}
