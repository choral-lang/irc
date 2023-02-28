package choral.examples.irc;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * The state is designed to be used concurrently by multiple threads.
 *
 * Some methods are only thread-safe if the invocations do not share the same
 * client. This is not an issue as the assumption is that only a single thread
 * is ever in charge of processing of a particular client and updating its
 * state.
 *
 * Other methods that affect multiple clients are thread-safe. However, in
 * certain cases it's still necessary to use explicit "synchronized" blocks if
 * one needs e.g. a stable snapshot of the list of users during an operation,
 * etc.
 */
public class ServerState {
    private String hostname;
    private AtomicLong lastClientId;
    private ConcurrentMap<Long, ServerClientState> clients;
    private ConcurrentMap<String, ServerClientState> nicknames;
    private ConcurrentMap<String, Set<ServerClientState>> channels;

    public ServerState(String hostname) {
        this.hostname = hostname;
        lastClientId = new AtomicLong(0);
        clients = new ConcurrentHashMap<>();
        nicknames = new ConcurrentHashMap<>();
        channels = new ConcurrentHashMap<>();
    }

    /**
     * Add a new client to the state.
     *
     * Return the client ID which makes it possible to use all of the other
     * methods of this class.
     *
     * This method is thread-safe.
     */
    public long newClient(IrcChannel_B ch, EventQueue<Message> serverQueue) {
        long clientId = lastClientId.addAndGet(1);
        ServerClientState client = new ServerClientState(
            ch, serverQueue, clientId);
        clients.put(clientId, client);
        return clientId;
    }

    /**
     * This method is thread-safe.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * This method is thread-safe.
     */
    public Set<Long> clients() {
        return clients.keySet();
    }

    /**
     * This method is thread-safe.
     */
    public void enqueue(long clientId, Message message) {
        assert clients.containsKey(clientId);
        clients.get(clientId).serverQueue.enqueue(message);
    }

    /**
     * This method is thread-safe.
     */
    public long getClientId(String nickname) {
        assert nicknames.containsKey(nickname);
        return nicknames.get(nickname).clientId;
    }

    /**
     * This method is thread-safe.
     */
    public String getUsername(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).username;
    }

    /**
     * This method is thread-safe as long as the clients differ.
     */
    public void setUsername(long clientId, String username) {
        assert clients.containsKey(clientId);
        clients.get(clientId).username = username;
    }

    /**
     * This method is thread-safe.
     */
    public String getRealname(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).realname;
    }

    /**
     * This method is thread-safe as long as the clients differ.
     */
    public void setRealname(long clientId, String realname) {
        assert clients.containsKey(clientId);
        clients.get(clientId).realname = realname;
    }

    /**
     * This method is thread-safe.
     */
    public String getNickname(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).nickname;
    }

    /**
     * This method is thread-safe as long as the clients differ.
     */
    public boolean setNickname(long clientId, String nickname) {
        assert clients.containsKey(clientId);
        ServerClientState client = clients.get(clientId);
        ServerClientState res = nicknames.putIfAbsent(nickname, client);

        if (res == null) {
            client.nickname = nickname;
        }

        return res == null;
    }

    /**
     * This method is thread-safe.
     */
    public boolean canRegister(long clientId) {
        assert clients.containsKey(clientId);
        ServerClientState client = clients.get(clientId);

        return client.username != null &&
            client.realname != null &&
            client.nickname != null;
    }

    /**
     * This method is thread-safe.
     */
    public boolean isRegistered(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).registered;
    }

    /**
     * This method is thread-safe as long as the clients differ.
     */
    public void setRegistered(long clientId) {
        assert clients.containsKey(clientId);
        clients.get(clientId).registered = true;
    }

    /**
     * This method is thread-safe.
     */
    public boolean nicknameExists(String nickname) {
        return nicknames.containsKey(nickname);
    }

    /**
     * This method is thread-safe.
     */
    public Set<String> getChannels(long clientId) {
        return new HashSet<>(clients.get(clientId).channels);
    }

    /**
     * This method is thread-safe.
     */
    public boolean channelExists(String channel) {
        return channels.containsKey(channel);
    }

    /**
     * This method is thread-safe.
     */
    public Set<Long> getMembers(String channel) {
        return channels.getOrDefault(channel, Set.of()).stream()
            .map(c -> c.clientId).collect(Collectors.toSet());
    }

    /**
     * This method is thread-safe.
     */
    public boolean inChannel(long clientId, String channel) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).channels.contains(channel);
    }

    /**
     * This method is thread-safe as long as the clients differ.
     */
    public void joinChannel(long clientId, String channel) {
        assert clients.containsKey(clientId);
        ServerClientState client = clients.get(clientId);

        client.channels.add(channel);
        channels.merge(channel, new HashSet<>(), (members, __) -> {
            Set<ServerClientState> res = new HashSet<>(members);
            res.add(client);
            return res;
        });
    }

    /**
     * This method is thread-safe as long as the clients differ.
     */
    public void partChannel(long clientId, String channel) {
        assert clients.containsKey(clientId);
        ServerClientState client = clients.get(clientId);

        client.channels.remove(channel);
        channels.compute(channel, (__, members) -> {
            Set<ServerClientState> res = new HashSet<>(members);
            res.remove(client);
            return res.isEmpty() ? null : res;
        });
    }

    /**
     * Remove the client from the state.
     *
     * This method should only ever be called once. After it completes, it is
     * invalid to use the same client ID with any of the method of this state.
     *
     * This method is thread-safe.
     */
    public void quit(long clientId) {
        assert clients.containsKey(clientId);

        synchronized (this) {
            ServerClientState client = clients.remove(clientId);

            if (client.nickname != null) {
                nicknames.remove(client.nickname);
            }

            for (String channel : client.channels) {
                channels.computeIfPresent(channel, (__, members) -> {
                    Set<ServerClientState> res = new HashSet<>(members);
                    res.remove(client);
                    return res.isEmpty() ? null : res;
                });
            }
        }
    }

    /**
     * This method is thread-safe.
     */
    public void setQuitRequested(long clientId) {
        assert clients.containsKey(clientId);
        clients.get(clientId).quitRequested.set(true);
    }

    /**
     * This method is thread-safe.
     */
    public boolean isQuitRequested(long clientId) {
        assert clients.containsKey(clientId);
        return clients.get(clientId).quitRequested.get();
    }

    /**
     * This method is thread-safe.
     */
    public void stop(long clientId) {
        assert clients.containsKey(clientId);
        clients.get(clientId).serverQueue.stop();
    }

    /**
     * This method is thread-safe.
     */
    public void close(long clientId) {
        assert clients.containsKey(clientId);
        clients.get(clientId).ch.close();
    }

    public PrintStream getOut() {
        return System.out;
    }
}
