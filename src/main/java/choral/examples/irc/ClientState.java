package choral.examples.irc;

import java.io.PrintStream;

public class ClientState {
    private String username, realname, nickname;
    private String lastNickname;

    public ClientState(String username, String realname, String nickname) {
        this.username = username;
        this.realname = realname;
        setNickname(nickname);
    }

    public ClientState(String nickname) {
        this(nickname, nickname, nickname);
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
        this.lastNickname = this.nickname;
        this.nickname = nickname;
    }

    public void revertNickname() {
        this.nickname = this.lastNickname;
    }

    public PrintStream getOut() {
        return System.out;
    }
}
