package choral.examples.irc;

public abstract class ClientEvent {
    private ClientEventType eventType;

    ClientEvent(ClientEventType eventType) {
        this.eventType = eventType;
    }

    public ClientEventType getType() {
        return eventType;
    }

    public ClientPingEvent asClientPingEvent() {
        assert eventType == ClientEventType.PING;
        return (ClientPingEvent) this;
    }

    public ClientPongEvent asClientPongEvent() {
        assert eventType == ClientEventType.PONG;
        return (ClientPongEvent) this;
    }

    public ClientNickEvent asClientNickEvent() {
        assert eventType == ClientEventType.NICK;
        return (ClientNickEvent) this;
    }

    public ClientUserEvent asClientUserEvent() {
        assert eventType == ClientEventType.USER;
        return (ClientUserEvent) this;
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
