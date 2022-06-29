package choral.examples.irc;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerClientState {
    public long clientId;
    public LinkedBlockingQueue<ServerEvent> queue;
    public String username, realname, nickname;
    public boolean welcomeDone;
    public Set<String> channels;

    ServerClientState(long clientId, LinkedBlockingQueue<ServerEvent> queue) {
        this.clientId = clientId;
        this.queue = queue;
        this.username = null;
        this.realname = null;
        this.nickname = null;
        this.welcomeDone = false;
        this.channels = new HashSet<>();
    }
}
