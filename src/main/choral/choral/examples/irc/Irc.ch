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

            if (event.getType() == ClientEventType@Client.NICK) {
                ch_AB.<ClientEventType>select(ClientEventType@Client.NICK);

                ClientNickEvent@Client e = event.asClientNickEvent();
                clientState.nickname = e.getNickname();

                Message@Client m = Message@Client.prepareNick(e.getNickname());
                Message@Server s = Message@Server.parse(ch_AB.<String>com(m.serialize()));
                serverState.nickname = s.getParam(0@Server);
                // TODO: Insert into server's queue to broadcast to other
                // clients.
            }
            else {
                ch_AB.<ClientEventType>select(ClientEventType@Client.REGISTER);

                ClientRegisterEvent@Client e = event.asClientRegisterEvent();
                clientState.username = e.getUsername();
                clientState.realname = e.getRealname();

                Message@Client m = Message@Client.prepareUser(e.getUsername(), e.getRealname());
                Message@Server s = Message@Server.parse(ch_AB.<String>com(m.serialize()));
                serverState.username = s.getParam(0@Server);
                serverState.realname = s.getParam(3@Server);
            }
        }
        catch (InterruptedException@Client e) {
            Thread@Client.currentThread().interrupt();
        }

        clientDrivenLoop();
    }

    private void serverDrivenLoop() {
        try {
            ServerEvent@Server event = serverQueue.take();
            ServerEventType@Server t = event.getType();

            if (event.getType() == ServerEventType@Server.NICK) {
                ch_AB.<ServerEventType>select(ServerEventType@Server.NICK);

                ServerNickEvent@Server e = event.asServerNickEvent();
                serverState.nickname = e.getNickname();

                Message@Server m = Message@Server.prepareNick(e.getNickname());
                Message@Client s = Message@Client.parse(ch_AB.<String>com(m.serialize()));
                // addServerEvent(new ServerNickEvent@Server(s.getParam(0@Server)));
            }
            else {
                ch_AB.<ServerEventType>select(ServerEventType@Server.REGISTER);

                ServerRegisterEvent@Server e = event.asServerRegisterEvent();
                serverState.username = e.getUsername();
                serverState.realname = e.getRealname();

                Message@Server m = Message@Server.prepareUser(e.getUsername(), e.getRealname());
                Message@Client s = Message@Client.parse(ch_AB.<String>com(m.serialize()));
                // addServerEvent(new ServerRegisterEvent@Server(s.getParam(0@Server), s.getParam(3@Server)));
            }
        }
        catch (InterruptedException@Server e) {
            Thread@Server.currentThread().interrupt();
        }

        serverDrivenLoop();
    }
}
