package choral.examples.irc;

public class ServerState {
    public String username, realname, nickname;

    public ServerState() {}

    public boolean nicknameInUse(String nickname) {
        return false;
    }
}
