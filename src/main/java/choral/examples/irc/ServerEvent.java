package choral.examples.irc;

public abstract class ServerEvent {
    private ServerEventType eventType;

    ServerEvent(ServerEventType eventType) {
        this.eventType = eventType;
    }

    public ServerEventType getType() {
        return eventType;
    }

    public ServerPingEvent asServerPingEvent() {
        assert eventType == ServerEventType.PING;
        return (ServerPingEvent) this;
    }

    public ServerPongEvent asServerPongEvent() {
        assert eventType == ServerEventType.PONG;
        return (ServerPongEvent) this;
    }

    public ServerNickEvent asServerNickEvent() {
        assert eventType == ServerEventType.NICK;
        return (ServerNickEvent) this;
    }

    public ServerNickErrorEvent asServerNickErrorEvent() {
        assert eventType == ServerEventType.NICK_ERROR;
        return (ServerNickErrorEvent) this;
    }

    public ServerUserErrorEvent asServerUserErrorEvent() {
        assert eventType == ServerEventType.USER_ERROR;
        return (ServerUserErrorEvent) this;
    }

    public ServerForwardMessageEvent asServerForwardMessageEvent() {
        assert eventType == ServerEventType.FORWARD_MESSAGE;
        return (ServerForwardMessageEvent) this;
    }

    public ServerMessageEvent asServerMessageEvent() {
        assert eventType == ServerEventType.MESSAGE;
        return (ServerMessageEvent)this;
    }

    public ServerJoinEvent asServerJoinEvent() {
        assert eventType == ServerEventType.JOIN;
        return (ServerJoinEvent)this;
    }

    public ServerPartEvent asServerPartEvent() {
        assert eventType == ServerEventType.PART;
        return (ServerPartEvent) this;
    }

    public ServerRplNamReplyEvent asServerRplNamReplyEvent() {
        assert eventType == ServerEventType.RPL_NAMREPLY;
        return (ServerRplNamReplyEvent) this;
    }
}
