package choral.examples.irc;

public abstract class ServerLocalEvent {
    private ServerLocalEventType eventType;

    ServerLocalEvent(ServerLocalEventType eventType) {
        this.eventType = eventType;
    }

    public ServerLocalEventType getType() {
        return eventType;
    }

    public ServerLocalCheckNickEvent asServerLocalCheckNickEvent() {
        assert eventType == ServerLocalEventType.CHECK_NICK;
        return (ServerLocalCheckNickEvent) this;
    }

    public ServerLocalCheckUserEvent asServerLocalCheckUserEvent() {
        assert eventType == ServerLocalEventType.CHECK_USER;
        return (ServerLocalCheckUserEvent) this;
    }

    public ServerLocalJoinEvent asServerLocalJoinEvent() {
        assert eventType == ServerLocalEventType.JOIN;
        return (ServerLocalJoinEvent) this;
    }

    public ServerLocalPartEvent asServerLocalPartEvent() {
        assert eventType == ServerLocalEventType.PART;
        return (ServerLocalPartEvent) this;
    }
}
