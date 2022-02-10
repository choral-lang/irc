package choral.examples.irc;

public class ClientUserEvent extends ClientEvent {
    private String realname, username;

    private ClientUserEvent() {
        super(ClientEventType.USER);
    }

    public ClientUserEvent(String username, String realname) {
        this();
        this.username = username;
        this.realname = realname;
    }

    public String getUsername() {
        return username;
    }

    public String getRealname() {
        return realname;
    }
}
