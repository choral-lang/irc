package choral.examples.irc;

public abstract class ServerEvent {
    private ServerEventType eventType;

    ServerEvent(ServerEventType eventType) {
        this.eventType = eventType;
    }

    public ServerEventType getType() {
        return eventType;
    }

    public ServerNickErrorEvent asServerNickErrorEvent() {
        assert eventType == ServerEventType.NICK_ERROR;
        return (ServerNickErrorEvent) this;
    }

    public ServerUserErrorEvent asServerUserErrorEvent() {
        assert eventType == ServerEventType.USER_ERROR;
        return (ServerUserErrorEvent) this;
    }

    public ServerMessageEvent asServerMessageEvent() {
        assert eventType == ServerEventType.MESSAGE;
        return (ServerMessageEvent)this;
    }

    public ServerJoinEvent asServerJoinEvent() {
        assert eventType == ServerEventType.JOIN;
        return (ServerJoinEvent)this;
    }
}
