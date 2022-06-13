package choral.examples.irc;

public class ServerPongEvent extends ServerEvent {
    private PingMessage message;

    public ServerPongEvent(PingMessage message) {
        super(ServerEventType.PONG);
        this.message = message;
    }

    public PingMessage getMessage() {
        return message;
    }
}
