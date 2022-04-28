package choral.examples.irc;

public class ServerLocalCheckUserEvent extends ServerLocalEvent {
    private String username;
    private String realname;

    public ServerLocalCheckUserEvent(String username, String realname) {
        super(ServerLocalEventType.CHECK_USER);
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
