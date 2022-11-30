package choral.examples.irc;

import java.util.concurrent.ExecutorService;

public class Irc@(Client, Server) {
    private Events@(Client, Server)<Message> events;
    private IrcChannel@(Client, Server) ch_AB;

    private ClientState@Client clientState;

    private ServerState@Server serverState;
    private long@Server clientId;

    public Irc(IrcChannel@(Client, Server) ch_AB,
               ClientState@Client clientState,
               ServerState@Server serverState) {
        this.events = new Events@(Client, Server)<Message>();
        this.ch_AB = ch_AB;

        this.clientState = clientState;

        this.serverState = serverState;
        this.clientId = serverState.newClient(events.queueB());
    }

    public void enqueue(Message@Client message) {
        events.queueA().enqueue(message);
    }

    public void enqueue(Message@Server message) {
        events.queueB().enqueue(message);
    }

    public void run(ExecutorService@Client clientExecutor,
                    ExecutorService@Server serverExecutor) {
        events.run(
            clientExecutor,
            serverExecutor,
            new IrcClientHandler@(Client, Server)(
                events.queueB(), ch_AB, clientState, serverState, clientId),
            new IrcServerHandler@(Client, Server)(
                events.queueA(), ch_AB, clientState, serverState, clientId),
            new IrcClientLocalHandler@Client(
                events.queueA(), clientState),
            new IrcServerLocalHandler@Server(
                events.queueB(), serverState, clientId));
    }
}
