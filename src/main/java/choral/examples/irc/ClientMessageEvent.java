package choral.examples.irc;

import java.util.List;

public class ClientMessageEvent extends ClientEvent {
    public List<String> targets;
    public String message;

    public ClientMessageEvent(List<String> targets, String message) {
        super(ClientEventType.MESSAGE);
        this.targets = targets;
        this.message = message;
    }
}
