package choral.examples.irc;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerClientState {
    public transient IrcChannel_B ch;
    public transient EventQueue<Message> serverQueue;
    public long clientId;
    public AtomicBoolean quitRequested;
    public volatile String username, realname, nickname;
    public volatile boolean registered;
    public Set<String> channels;

    ServerClientState(IrcChannel_B ch, EventQueue<Message> serverQueue,
                      long clientId) {
        this.ch = ch;
        this.serverQueue = serverQueue;
        this.clientId = clientId;
        this.quitRequested = new AtomicBoolean(false);
        this.username = null;
        this.realname = null;
        this.nickname = null;
        this.registered = false;
        this.channels = new HashSet<>();
    }
}
