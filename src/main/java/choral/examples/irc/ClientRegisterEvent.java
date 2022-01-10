package choral.examples.irc;

public class ClientRegisterEvent extends ClientEvent {
    private String realname, username;

    private ClientRegisterEvent() {
        super(ClientEventType.REGISTER);
    }

    public ClientRegisterEvent(String username, String realname) {
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
