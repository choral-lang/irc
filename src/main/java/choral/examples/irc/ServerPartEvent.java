package choral.examples.irc;

public class ServerPartEvent extends ServerEvent {
    private PartMessage message;

    public ServerPartEvent(PartMessage message) {
        super(ServerEventType.PART);
        this.message = message;
    }

    public PartMessage getMessage() {
        return message;
    }
}
