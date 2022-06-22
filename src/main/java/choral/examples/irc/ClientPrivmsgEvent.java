package choral.examples.irc;

public class ClientPrivmsgEvent extends ClientEvent {
    private PrivmsgMessage message;

    public ClientPrivmsgEvent(PrivmsgMessage message) {
        super(ClientEventType.PRIVMSG);
        this.message = message;
    }

    public PrivmsgMessage getMessage() {
        return message;
    }
}
