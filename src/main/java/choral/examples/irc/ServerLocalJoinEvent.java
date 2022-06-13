package choral.examples.irc;

public class ServerLocalJoinEvent extends ServerLocalEvent {
    private JoinMessage message;

    public ServerLocalJoinEvent(JoinMessage message) {
        super(ServerLocalEventType.JOIN);
        this.message = message;
    }

    public JoinMessage getMessage() {
        return message;
    }
}
