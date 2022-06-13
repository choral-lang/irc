package choral.examples.irc;

public class ServerPingEvent extends ServerEvent {
    private String token;

    public ServerPingEvent(String token) {
        super(ServerEventType.PING);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
