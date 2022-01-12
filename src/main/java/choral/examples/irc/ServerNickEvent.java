package choral.examples.irc;

public class ServerNickEvent extends ServerEvent {
    private String nickname;

    private ServerNickEvent() {
        super(ServerEventType.NICK);
    }

    public ServerNickEvent(String nickname) {
        this();
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
