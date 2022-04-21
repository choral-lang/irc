package choral.examples.irc;

public class ServerNickEvent extends ServerEvent {
    private String nickname;

    public ServerNickEvent(String nickname) {
        super(ServerEventType.NICK);
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
