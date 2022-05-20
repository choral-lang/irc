package choral.examples.irc;

public class ServerForwardMessageEvent extends ServerEvent {
    private Message message;

    public ServerForwardMessageEvent(Message message) {
        super(ServerEventType.FORWARD_MESSAGE);
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
