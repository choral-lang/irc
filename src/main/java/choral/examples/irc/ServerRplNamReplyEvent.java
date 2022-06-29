package choral.examples.irc;

public class ServerRplNamReplyEvent extends ServerEvent {
    private RplNamReplyMessage message;

    public ServerRplNamReplyEvent(RplNamReplyMessage message) {
        super(ServerEventType.RPL_NAMREPLY);
        this.message = message;
    }

    public RplNamReplyMessage getMessage() {
        return message;
    }
}
