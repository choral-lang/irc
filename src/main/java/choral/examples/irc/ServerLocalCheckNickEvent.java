package choral.examples.irc;

public class ServerLocalCheckNickEvent extends ServerLocalEvent {
    private NickMessage message;

    public ServerLocalCheckNickEvent(NickMessage message) {
        super(ServerLocalEventType.CHECK_NICK);
        this.message = message;
    }

    public NickMessage getMessage() {
        return message;
    }
}
