package choral.examples.irc;

public class ServerNickErrorEvent extends ServerEvent {
    private NickMessage origin;
    private Message reply;

    public ServerNickErrorEvent(NickMessage origin, Message reply) {
        super(ServerEventType.NICK_ERROR);
        this.origin = origin;
        this.reply = reply;
    }

    public NickMessage getOrigin() {
        return origin;
    }

    public Message getReply() {
        return reply;
    }
}
