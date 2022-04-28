package choral.examples.irc;

import java.io.PrintStream;

public class ServerState {
    private String username, realname, nickname;

    public ServerState() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean nicknameInUse(String nickname) {
        return false;
    }

    public boolean usernameRegistered(String username) {
        return false;
    }

    public boolean isRegistrationDone() {
        return (this.username != null && this.realname != null &&
                this.nickname != null);
    }

    public PrintStream getOut() {
        return System.out;
    }
}
