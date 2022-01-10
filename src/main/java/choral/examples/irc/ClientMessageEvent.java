package choral.examples.irc;

import java.util.List;

public class ClientMessageEvent extends ClientEvent {
    private List<String> targets;
    private String message;

    public ClientMessageEvent(List<String> targets, String message) {
        super(ClientEventType.MESSAGE);
        this.targets = targets;
        this.message = message;
    }

    public List<String> getTargets() {
        return targets;
    }

    public String getMessage() {
        return message;
    }
}
