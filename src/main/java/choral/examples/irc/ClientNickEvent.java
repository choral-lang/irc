package choral.examples.irc;

public class ClientNickEvent extends ClientEvent {
    private String nickname;

    private ClientNickEvent() {
        super(ClientEventType.NICK);
    }

    public ClientNickEvent(String nickname) {
        this();
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }
}
