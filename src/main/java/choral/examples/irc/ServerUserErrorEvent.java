package choral.examples.irc;

public class ServerUserErrorEvent extends ServerEvent {
    private UserMessage origin;
    private Message reply;

    public ServerUserErrorEvent(UserMessage origin, Message reply) {
        super(ServerEventType.USER_ERROR);
        this.origin = origin;
        this.reply = reply;
    }

    public UserMessage getOrigin() {
        return origin;
    }

    public Message getReply() {
        return reply;
    }
}
