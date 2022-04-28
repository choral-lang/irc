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
}
