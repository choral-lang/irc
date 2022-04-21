package choral.examples.irc;

public class ServerUserEvent extends ServerEvent {
    private String realname, username;

    public ServerUserEvent(String username, String realname) {
        super(ServerEventType.USER);
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
