package choral.examples.irc;

import java.util.HashSet;
import java.util.Set;

public class ServerClientState {
    public long clientId;
    public String username, realname, nickname;
    public boolean welcomeDone;
    public Set<String> channels;

    ServerClientState(long clientId) {
        this.clientId = clientId;
        this.username = null;
        this.realname = null;
        this.nickname = null;
        this.welcomeDone = false;
        this.channels = new HashSet<>();
    }
}
