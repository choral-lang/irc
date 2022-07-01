package choral.examples.irc;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientState {
    private String username, realname, nickname;
    private Map<String, Set<String>> channels;

    public ClientState(String username, String realname, String nickname) {
        this.username = username;
        this.realname = realname;
        this.nickname = nickname;
        channels = new HashMap<>();
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
        this.nickname = nickname;
    }

    public Set<String> getChannels() {
        return new HashSet<>(channels.keySet());
    }

    public boolean inChannel(String channel) {
        return channels.containsKey(channel);
    }

    public void joinChannel(String channel) {
        assert !channels.containsKey(channel);
        channels.put(channel, new HashSet<>());
    }

    public void partChannel(String channel) {
        assert channels.containsKey(channel);
        channels.remove(channel);
    }

    public void addMember(String channel, String nickname) {
        assert channels.containsKey(channel);
        channels.get(channel).add(nickname);
    }

    public void renameMember(String from, String to) {
        for (Set<String> members : channels.values()) {
            assert members.contains(from) && !members.contains(to);
            members.remove(from);
            members.add(to);
        }
    }

    public void removeMember(String channel, String nickname) {
        assert channels.containsKey(channel);
        channels.get(channel).remove(nickname);
    }

    public PrintStream getOut() {
        return System.out;
    }
}
