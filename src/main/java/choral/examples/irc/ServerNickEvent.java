package choral.examples.irc;

public class ServerNickEvent extends ServerEvent {
    private String nickname;
    private Message error;

    public ServerNickEvent(String nickname, Message error) {
        super(ServerEventType.NICK);
        this.nickname = nickname;
        this.error = error;
    }

    public String getNickname() {
        return nickname;
    }

    public Message getError() {
        return error;
    }
}
