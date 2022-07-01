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
    private Map<String, ServerClientState> nicknames;
    private Map<String, Set<ServerClientState>> channels;

    public ServerState() {
        lastClientId = 0;
        clients = new HashMap<>();
        nicknames = new HashMap<>();
        channels = new HashMap<>();
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

    public long getClientId(String nickname) {
        return nicknames.get(nickname).clientId;
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
        ServerClientState client = clients.get(clientId);

        nicknames.remove(client.nickname);
        client.nickname = nickname;
        nicknames.put(nickname, client);
    }

    public boolean canRegister(long clientId) {
        assert clients.containsKey(clientId);
        ServerClientState client = clients.get(clientId);

        return client.username != null &&
            client.realname != null &&
            client.nickname != null;
    }

    public boolean isRegistered(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).registered;
    }

    public void setRegistered(long clientId) {
        assert clients.containsKey(clientId);
        clients.get(clientId).registered = true;
    }

    public boolean nicknameExists(String nickname) {
        return nicknames.containsKey(nickname);
    }

    public Set<String> getChannels(long clientId) {
        return new HashSet<>(clients.get(clientId).channels);
    }

    public boolean channelExists(String channel) {
        return channels.containsKey(channel);
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
