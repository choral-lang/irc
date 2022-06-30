package choral.examples.irc;

public class ClientJoinEvent extends ClientEvent {
    private JoinMessage message;

    public ClientJoinEvent(JoinMessage message) {
        super(ClientEventType.JOIN);
        this.message = message;
    }

    public JoinMessage getMessage() {
        return message;
    }
}
