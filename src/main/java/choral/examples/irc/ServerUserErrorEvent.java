package choral.examples.irc;

public class ServerUserErrorEvent extends ServerEvent {
    private String realname, username;
    private Message error;

    public ServerUserErrorEvent(String username, String realname, Message error) {
        super(ServerEventType.USER_ERROR);
        this.username = username;
        this.realname = realname;
        this.error = error;
    }

    public String getUsername() {
        return username;
    }

    public String getRealname() {
        return realname;
    }

    public Message getError() {
        return error;
    }
}
