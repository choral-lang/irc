package choral.examples.irc;

public class ClientPongEvent extends ClientEvent {
    private String token;

    public ClientPongEvent(String token) {
        super(ClientEventType.PONG);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
