package choral.examples.irc;

import java.util.concurrent.ExecutorService;

public class Irc@(Client, Server) {
    private Events@(Client, Server)<Message> events;
    private IrcChannel@(Client, Server) ch_AB;

    public Irc(IrcChannel@(Client, Server) ch_AB) {
        events = new Events@(Client, Server)<Message>();
        this.ch_AB = ch_AB;
    }

    public EventQueue@Client<Message> clientQueue() {
        return events.queueA();
    }

    public EventQueue@Server<Message> serverQueue() {
        return events.queueB();
    }

    public void enqueue(Message@Client message) {
        clientQueue().enqueue(message);
    }

    public void enqueue(Message@Server message) {
        serverQueue().enqueue(message);
    }

    public void run(ClientState@Client clientState,
                    ServerState@Server serverState,
                    long@Server clientId,
                    ExecutorService@Client clientExecutor,
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
