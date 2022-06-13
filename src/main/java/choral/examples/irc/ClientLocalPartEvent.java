package choral.examples.irc;

public class ClientLocalPartEvent extends ClientLocalEvent {
    private PartMessage message;

    public ClientLocalPartEvent(PartMessage message) {
        super(ClientLocalEventType.PART);
        this.message = message;
    }

    public PartMessage getMessage() {
        return message;
    }
}
