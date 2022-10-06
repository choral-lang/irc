package choral.examples.irc;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerClientState {
    public long clientId;
    public LinkedBlockingQueue<Message> queue;
    public boolean gracefulQuit;
    public String username, realname, nickname;
    public boolean registered;
    public Set<String> channels;

    ServerClientState(long clientId) {
        this.clientId = clientId;
        this.queue = new LinkedBlockingQueue<Message>();
        this.gracefulQuit = false;
        this.username = null;
        this.realname = null;
        this.nickname = null;
        this.registered = false;
        this.channels = new HashSet<>();
    }
}
