package choral.examples.irc;

import java.io.PrintStream;

public class ServerState {
    private String username, realname, nickname;
    private boolean welcomeDone;

    public ServerState() {
        this.username = null;
        this.realname = null;
        this.nickname = null;
        this.welcomeDone = false;
    }

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

    public boolean isWelcomeDone() {
        return welcomeDone;
    }

    public void setWelcomeDone(boolean welcomeDone) {
        this.welcomeDone = welcomeDone;
    }

    public boolean nicknameInUse(String nickname) {
        return false;
    }

    public boolean usernameRegistered(String username) {
        return false;
    }

    public boolean isRegistered() {
        return this.username != null &&
               this.realname != null &&
               this.nickname != null;
    }

    public PrintStream getOut() {
        return System.out;
    }
}
