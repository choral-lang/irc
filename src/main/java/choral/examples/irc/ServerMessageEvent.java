package choral.examples.irc;

import java.util.List;

public class ServerMessageEvent extends ServerEvent {
    private List<String> targets;
    private String message;

    public ServerMessageEvent(List<String> targets, String message) {
        super(ServerEventType.MESSAGE);
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
