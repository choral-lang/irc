package choral.examples.irc;

import java.io.PrintStream;
import java.util.Set;
import java.util.HashSet;

public class ClientState {
    private String username, realname, nickname;
    private String lastNickname;
    private Set<String> channels;

    public ClientState(String username, String realname, String nickname) {
        this.username = username;
        this.realname = realname;
        this.nickname = nickname;
        this.lastNickname = null;
        this.channels = new HashSet<>();
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

    public Set<String> getChannels() {
        return new HashSet<>(channels);
    }

    public boolean inChannel(String channel) {
        return channels.contains(channel);
    }

    public void joinChannel(String channel) {
        channels.add(channel);
    }

    public void partChannel(String channel) {
        channels.remove(channel);
    }

    public PrintStream getOut() {
        return System.out;
    }
}
