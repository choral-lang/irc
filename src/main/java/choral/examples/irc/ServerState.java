package choral.examples.irc;

import java.io.PrintStream;
import java.util.Set;
import java.util.HashSet;

public class ServerState {
    private String username, realname, nickname;
    private boolean welcomeDone;
    private Set<String> channels;

    public ServerState() {
        this.username = null;
        this.realname = null;
        this.nickname = null;
        this.welcomeDone = false;
        this.channels = new HashSet<>();
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
