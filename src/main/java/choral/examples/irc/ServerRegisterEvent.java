package choral.examples.irc;

public class ServerRegisterEvent extends ServerEvent {
    private String realname, username;

    private ServerRegisterEvent() {
        super(ServerEventType.REGISTER);
    }

    public ServerRegisterEvent(String username, String realname) {
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
