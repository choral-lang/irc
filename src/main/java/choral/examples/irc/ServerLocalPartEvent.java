package choral.examples.irc;

public class ServerLocalPartEvent extends ServerLocalEvent {
    private PartMessage message;

    public ServerLocalPartEvent(PartMessage message) {
        super(ServerLocalEventType.PART);
        this.message = message;
    }

    public PartMessage getMessage() {
        return message;
    }
}
