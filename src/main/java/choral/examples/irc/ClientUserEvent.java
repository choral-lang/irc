package choral.examples.irc;

public class ClientUserEvent extends ClientEvent {
    private UserMessage message;

    public ClientUserEvent(UserMessage message) {
        super(ClientEventType.USER);
        this.message = message;
    }

    public UserMessage getMessage() {
        return message;
    }
}
