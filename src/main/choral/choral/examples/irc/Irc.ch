package choral.examples.irc;

import java.util.concurrent.ExecutorService;

public class Irc@(Client, Server) {
    private Loops@(Client, Server)<Message> loops;
    private IrcChannel@(Client, Server) ch_AB;

    private ClientState@Client clientState;

    private ServerState@Server serverState;
    private long@Server clientId;

    public Irc(IrcChannel@(Client, Server) ch_AB,
               ClientState@Client clientState,
               ServerState@Server serverState) {
        this.loops = new Loops@(Client, Server)<Message>();
        this.ch_AB = ch_AB;

        this.clientState = clientState;

        this.serverState = serverState;
        this.clientId = serverState.newClient(loops.getLoopB());
    }

    public void addClientMessage(Message@Client message) {
        loops.getLoopA().add(message);
    }

    public void addServerMessage(Message@Server message) {
        loops.getLoopB().add(message);
    }

    public void run(ExecutorService@Client clientExecutor,
                    ExecutorService@Server serverExecutor) {
        loops.run(
            clientExecutor,
            serverExecutor,
            new IrcClientStep@(Client, Server)(
                loops.getLoopB(), ch_AB, clientState, serverState, clientId),
            new IrcServerStep@(Client, Server)(
                loops.getLoopA(), ch_AB, clientState, serverState, clientId),
            new IrcClientHandler@Client(
                loops.getLoopA(), clientState),
            new IrcServerHandler@Server(
                loops.getLoopB(), serverState, clientId));
    }
}
