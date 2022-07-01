package choral.examples.irc;

public class ServerNickEvent extends ServerEvent {
    private NickMessage message;

    public ServerNickEvent(NickMessage message) {
        super(ServerEventType.NICK);
        this.message = message;
    }

    public NickMessage getMessage() {
        return message;
    }
}
