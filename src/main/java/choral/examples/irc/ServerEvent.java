package choral.examples.irc;

public abstract class ServerEvent {
    private ServerEventType eventType;

    ServerEvent(ServerEventType eventType) {
        this.eventType = eventType;
    }

    public ServerEventType getType() {
        return eventType;
    }

    public ServerNickEvent asServerNickEvent() {
        assert eventType == ServerEventType.NICK;
        return (ServerNickEvent) this;
    }

    public ServerRegisterEvent asServerRegisterEvent() {
        assert eventType == ServerEventType.REGISTER;
        return (ServerRegisterEvent) this;
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
