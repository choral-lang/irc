package choral.examples.irc;

public class ServerLocalCheckUserEvent extends ServerLocalEvent {
    private UserMessage message;

    public ServerLocalCheckUserEvent(UserMessage message) {
        super(ServerLocalEventType.CHECK_USER);
        this.message = message;
    }

    public UserMessage getMessage() {
        return message;
    }
}
