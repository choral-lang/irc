package choral.examples.irc;

import java.io.PrintStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class ServerState {
    private long lastClientId;
    private Map<Long, ServerClientState> clients;
    private Map<String, ServerClientState> channels;

    public ServerState() {
        this.lastClientId = 0;
        this.clients = new HashMap<>();
        this.channels = new HashMap<>();
    }

    public long newClient() {
        long clientId = ++lastClientId;
        ServerClientState client = new ServerClientState(clientId);
        clients.put(clientId, client);
        return clientId;
    }

    public String getUsername(long clientId) {
        return clients.get(clientId).username;
    }

    public void setUsername(long clientId, String username) {
        clients.get(clientId).username = username;
    }

    public String getRealname(long clientId) {
        return clients.get(clientId).realname;
    }

    public void setRealname(long clientId, String realname) {
        clients.get(clientId).realname = realname;
    }

    public String getNickname(long clientId) {
        return clients.get(clientId).nickname;
    }

    public void setNickname(long clientId, String nickname) {
        this.clients.get(clientId).nickname = nickname;
    }

    public boolean isWelcomeDone(long clientId) {
        return clients.get(clientId).welcomeDone;
    }

    public void setWelcomeDone(long clientId, boolean welcomeDone) {
        this.clients.get(clientId).welcomeDone = welcomeDone;
    }

    public boolean nicknameInUse(String nickname) {
        return clients.entrySet().stream()
            .anyMatch(e -> e.getValue().nickname == nickname);
    }

    public boolean isRegistered(long clientId) {
        ServerClientState client = clients.get(clientId);
        return client.username != null &&
               client.realname != null &&
               client.nickname != null;
    }

    public Set<String> getChannels(long clientId) {
        return new HashSet<>(clients.get(clientId).channels);
    }

    public boolean inChannel(long clientId, String channel) {
        return clients.get(clientId).channels.contains(channel);
    }

    public void joinChannel(long clientId, String channel) {
        clients.get(clientId).channels.add(channel);
    }

    public void partChannel(long clientId, String channel) {
        clients.get(clientId).channels.remove(channel);
    }

    public PrintStream getOut() {
        return System.out;
    }
}
