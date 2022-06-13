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

    public ClientLocalJoinEvent asClientLocalJoinEvent() {
        assert eventType == ClientLocalEventType.JOIN;
        return (ClientLocalJoinEvent) this;
    }

    public ClientLocalPartEvent asClientLocalPartEvent() {
        assert eventType == ClientLocalEventType.PART;
        return (ClientLocalPartEvent) this;
    }
}
