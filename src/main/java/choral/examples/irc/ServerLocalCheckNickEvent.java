package choral.examples.irc;

public class ServerLocalCheckNickEvent extends ServerLocalEvent {
    private String nickname;

    public ServerLocalCheckNickEvent(String nickname) {
        super(ServerLocalEventType.CHECK_NICK);
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
