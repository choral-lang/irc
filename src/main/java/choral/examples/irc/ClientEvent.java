package choral.examples.irc;

public abstract class ClientEvent {
    private ClientEventType eventType;

    ClientEvent(ClientEventType eventType) {
        this.eventType = eventType;
    }

    public ClientEventType getType() {
        return eventType;
    }

    public ClientMessageEvent asClientMessageEvent() {
        assert eventType == ClientEventType.MESSAGE;
        return (ClientMessageEvent)this;
    }

    public ClientJoinEvent asClientJoinEvent() {
        assert eventType == ClientEventType.JOIN;
        return (ClientJoinEvent)this;
    }
}
