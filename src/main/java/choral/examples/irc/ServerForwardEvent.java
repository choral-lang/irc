package choral.examples.irc;

public class ServerForwardEvent extends ServerEvent {
    private ForwardMessage message;

    public ServerForwardEvent(ForwardMessage message) {
        super(ServerEventType.FORWARD);
        this.message = message;
    }

    public ForwardMessage getMessage() {
        return message;
    }
}
