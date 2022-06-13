package choral.examples.irc;

public class ClientLocalJoinEvent extends ClientLocalEvent {
    private JoinMessage message;

    public ClientLocalJoinEvent(JoinMessage message) {
        super(ClientLocalEventType.JOIN);
        this.message = message;
    }

    public JoinMessage getMessage() {
        return message;
    }
}
