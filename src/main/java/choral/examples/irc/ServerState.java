package choral.examples.irc;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    public long newClient(LoopsLoop<Message> serverLoop) {
        long clientId = ++lastClientId;
        ServerClientState client = new ServerClientState(serverLoop, clientId);
        clients.put(clientId, client);
        return clientId;
    }

    public void addMessage(long clientId, Message message) {
        clients.get(clientId).serverLoop.add(message);
    }

    public long getClientId(String nickname) {
        assert nicknames.containsKey(nickname);
        return nicknames.get(nickname).clientId;
    }

    public String getUsername(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).username;
    }

    public void setUsername(long clientId, String username) {
        assert clients.containsKey(clientId);
        clients.get(clientId).username = username;
    }

    public String getRealname(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).realname;
    }

    public void setRealname(long clientId, String realname) {
        assert clients.containsKey(clientId);
        clients.get(clientId).realname = realname;
    }

    public String getNickname(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).nickname;
    }

    public void setNickname(long clientId, String nickname) {
        assert clients.containsKey(clientId);
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
        assert clients.containsKey(clientId);
        return clients.get(clientId).channels.contains(channel);
    }

    public void joinChannel(long clientId, String channel) {
        assert clients.containsKey(clientId);
        ServerClientState client = clients.get(clientId);

        client.channels.add(channel);
        channels.computeIfAbsent(channel, k -> new HashSet<>()).add(client);
    }

    public void partChannel(long clientId, String channel) {
        assert clients.containsKey(clientId);
        ServerClientState client = clients.get(clientId);

        client.channels.remove(channel);

        Set<ServerClientState> members = channels.get(channel);
        members.remove(client);

        if (members.isEmpty()) {
            channels.remove(channel);
        }
    }

    public void quit(long clientId) {
        assert clients.containsKey(clientId);
        ServerClientState client = clients.remove(clientId);
        nicknames.remove(client.nickname);

        for (String channel : client.channels) {
            Set<ServerClientState> members = channels.get(channel);
            members.remove(client);

            if (members.isEmpty()) {
                channels.remove(channel);
            }
        }
    }

    public void setQuitRequested(long clientId) {
        assert clients.containsKey(clientId);
        clients.get(clientId).quitRequested = true;
    }

    public boolean isQuitRequested(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).quitRequested;
    }

    public PrintStream getOut() {
        return System.out;
    }
}
