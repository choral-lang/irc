package choral.examples.irc;

public class ServerUserEvent extends ServerEvent {
    private String realname, username;

    private ServerUserEvent() {
        super(ServerEventType.USER);
    }

    public ServerUserEvent(String username, String realname) {
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
