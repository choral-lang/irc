package choral.examples.irc;

import choral.channels.SymChannel;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Irc@(Client, Server) {
    private SymChannel@(Client, Server)<String> ch_AB;

    private ClientState@Client clientState;
    private LinkedBlockingQueue@Client<ClientEvent> clientQueue;

    private ServerState@Server serverState;
    private LinkedBlockingQueue@Server<ServerEvent> serverQueue;

    public Irc(SymChannel@(Client, Server)<String> ch_AB) {
        this.ch_AB = ch_AB;
        this.clientState = new ClientState@Client();
        this.clientQueue = new LinkedBlockingQueue@Client<ClientEvent>();
        this.serverState = new ServerState@Server();
        this.serverQueue = new LinkedBlockingQueue@Server<ServerEvent>();
    }

    public void run() {
        // TODO: Start clientDrivenLoop() and serverDrivenLoop() on separate threads.
    }

    private void clientDrivenLoop() {
        try {
            ClientEvent@Client event = clientQueue.take();
            ClientEventType@Client t = event.getType();

            if (event.getType() == ClientEventType@Client.JOIN) {
                ch_AB.<ClientEventType>select(ClientEventType@Client.JOIN);
                ClientJoinEvent@Client joinEvent = event.asClientJoinEvent();
                Message@Client m = Message@Client.prepareJoin(joinEvent.getChannels(),
                                                              new ArrayList@Client<String>());
                ch_AB.<String>com(m.serialize());

                // TODO: The server has to reply.
            }
            else {
                ch_AB.<ClientEventType>select(ClientEventType@Client.MESSAGE);
            }
        }
        catch (InterruptedException@Client e) {
            Thread@Client.currentThread().interrupt();
        }

        clientDrivenLoop();
    }

    private void serverDrivenLoop() {
    }
}
