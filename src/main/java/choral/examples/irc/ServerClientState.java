package choral.examples.irc;

import java.util.HashSet;
import java.util.Set;

public class ServerClientState {
    public IrcChannel_B ch;
    public EventQueue<Message> serverQueue;
    public long clientId;
    public boolean quitRequested;
    public String username, realname, nickname;
    public boolean registered;
    public Set<String> channels;

    ServerClientState(IrcChannel_B ch, EventQueue<Message> serverQueue,
                      long clientId) {
        this.ch = ch;
        this.serverQueue = serverQueue;
        this.clientId = clientId;
        this.quitRequested = false;
        this.username = null;
        this.realname = null;
        this.nickname = null;
        this.registered = false;
        this.channels = new HashSet<>();
    }
}
