package choral.examples.irc;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClientState {
    private transient IrcChannel_A ch;
    private boolean quitRequested;
    private String username, realname, nickname;
    private boolean registered;
    private Map<String, Set<String>> channels;

    public ClientState(IrcChannel_A ch, String username, String realname, String nickname) {
        this.ch = ch;
        this.quitRequested = false;
        this.username = username;
        this.realname = realname;
        this.nickname = nickname;
        registered = false;
        channels = new HashMap<>();
    }

    public ClientState(IrcChannel_A ch, String nickname) {
        this(ch, nickname, nickname, nickname);
    }

    public boolean isQuitRequested() {
        return quitRequested;
    }

    public void setQuitRequested() {
        quitRequested = true;
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

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered() {
        registered = true;
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

    public void addMembers(String channel, List<String> nicknames) {
        assert channels.containsKey(channel);
        Set<String> members = channels.get(channel);

        for (String nickname : nicknames) {
            if (!nickname.equals(this.nickname)) {
                members.add(nickname);
            }
        }
    }

    public void addMember(String channel, String nickname) {
        addMembers(channel, List.of(nickname));
    }

    public void renameMember(String from, String to) {
        for (Set<String> members : channels.values()) {
            if (members.remove(from)) {
                members.add(to);
            }
        }
    }

    public void removeMember(String channel, String nickname) {
        assert channels.containsKey(channel);
        channels.get(channel).remove(nickname);
    }

    public void removeMember(String nickname) {
        for (Set<String> members : channels.values()) {
            members.remove(nickname);
        }
    }

    public void close() {
        ch.close();
    }

    public PrintStream getOut() {
        return System.out;
    }
}
