package choral.examples.irc;

public class ServerJoinEvent extends ServerEvent {
    private JoinMessage message;

    public ServerJoinEvent(JoinMessage message) {
        super(ServerEventType.JOIN);
        this.message = message;
    }

    public JoinMessage getMessage() {
        return message;
    }
}
