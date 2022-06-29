package choral.examples.irc;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class ServerState {
    private long lastClientId;
    private Map<Long, ServerClientState> clients;
    private Map<String, Set<ServerClientState>> channels;

    public ServerState() {
        this.lastClientId = 0;
        this.clients = new HashMap<>();
        this.channels = new HashMap<>();
    }

    public long newClient(LinkedBlockingQueue<ServerEvent> queue) {
        long clientId = ++lastClientId;
        ServerClientState client = new ServerClientState(clientId, queue);
        clients.put(clientId, client);
        return clientId;
    }

    public void addEvent(long clientId, ServerEvent event) {
        Util.<ServerEvent>put(clients.get(clientId).queue, event);
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
        return clients.values().stream().anyMatch(c -> c.nickname == nickname);
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

    public Set<Long> getMembers(String channel) {
        return channels.getOrDefault(channel, Set.of()).stream()
            .map(c -> c.clientId).collect(Collectors.toSet());
    }

    public boolean inChannel(long clientId, String channel) {
        return clients.get(clientId).channels.contains(channel);
    }

    public void joinChannel(long clientId, String channel) {
        ServerClientState client = clients.get(clientId);
        client.channels.add(channel);
        channels.computeIfAbsent(channel, k -> new HashSet<>()).add(client);
    }

    public void partChannel(long clientId, String channel) {
        ServerClientState client = clients.get(clientId);
        client.channels.remove(channel);

        Set<ServerClientState> members = channels.get(channel);
        members.remove(client);

        if (members.isEmpty()) {
            channels.remove(channel);
        }
    }

    public PrintStream getOut() {
        return System.out;
    }
}
