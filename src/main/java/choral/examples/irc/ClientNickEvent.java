package choral.examples.irc;

public class ClientNickEvent extends ClientEvent {
    private NickMessage message;

    public ClientNickEvent(NickMessage message) {
        super(ClientEventType.NICK);
        this.message = message;
    }

    public NickMessage getMessage() {
        return message;
    }
}
