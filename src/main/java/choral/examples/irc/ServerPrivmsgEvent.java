package choral.examples.irc;

public class ServerPrivmsgEvent extends ServerEvent {
    private PrivmsgMessage message;

    public ServerPrivmsgEvent(PrivmsgMessage message) {
        super(ServerEventType.PRIVMSG);
        this.message = message;
    }

    public PrivmsgMessage getMessage() {
        return message;
    }
}
