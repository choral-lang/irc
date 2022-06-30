package choral.examples.irc;

import java.util.List;

public class ClientPartEvent extends ClientEvent {
    private PartMessage message;

    public ClientPartEvent(PartMessage message) {
        super(ClientEventType.PART);
        this.message = message;
    }

    public PartMessage getMessage() {
        return message;
    }
}
