package choral.examples.irc;

public class ServerNickEvent extends ServerEvent {
    private NickMessage message;

    public ServerNickEvent(NickMessage message) {
        super(ServerEventType.NICK);
    }

    public NickMessage getMessage() {
        return message;
    }
}
