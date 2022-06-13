package choral.examples.irc;

public class ClientPingEvent extends ClientEvent {
    private String token;

    public ClientPingEvent(String token) {
        super(ClientEventType.PING);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
