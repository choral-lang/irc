package choral.examples.irc;

public abstract class ClientLocalEvent {
    private ClientLocalEventType eventType;

    ClientLocalEvent(ClientLocalEventType eventType) {
        this.eventType = eventType;
    }

    public ClientLocalEventType getType() {
        return eventType;
    }

    public ClientLocalPongEvent asClientLocalPongEvent() {
        assert eventType == ClientLocalEventType.PONG;
        return (ClientLocalPongEvent) this;
    }
}
