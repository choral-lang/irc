package choral.examples.irc;

public class ServerNickErrorEvent extends ServerEvent {
    private String nickname;
    private Message error;

    public ServerNickErrorEvent(String nickname, Message error) {
        super(ServerEventType.NICK_ERROR);
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
