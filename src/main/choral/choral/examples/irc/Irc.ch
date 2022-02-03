package choral.examples.irc;

import choral.channels.SymChannel;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class Irc@(Client, Server) {
    private SymChannel@(Client, Server)<Object> ch_AB;

    private ClientState@Client clientState;
    private LinkedBlockingQueue@Client<ClientEvent> clientQueue;

    private ServerState@Server serverState;
    private LinkedBlockingQueue@Server<ServerEvent> serverQueue;

    public Irc(SymChannel@(Client, Server)<Object> ch_AB) {
        this.ch_AB = ch_AB;
        this.clientState = new ClientState@Client();
        this.clientQueue = new LinkedBlockingQueue@Client<ClientEvent>();
        this.serverState = new ServerState@Server();
        this.serverQueue = new LinkedBlockingQueue@Server<ServerEvent>();
    }

    private ClientEvent@Client takeClientEvent() {
        try {
            return clientQueue.take();
        }
        catch (InterruptedException@Client e) {
            // Ignore the interrupt and try again.
            return takeClientEvent();
        }
    }

    private ServerEvent@Server takeServerEvent() {
        try {
            return serverQueue.take();
        }
        catch (InterruptedException@Server e) {
            // Ignore the interrupt and try again.
            return takeServerEvent();
        }
    }

	private Integer@Client addClientEvent(ClientEvent@Client event) {
        try {
			clientQueue.put(event);
            return null@Client;
        }
        catch (InterruptedException@Client e) {
            // Ignore the interrupt and try again.
            return addClientEvent(event);
        }
	}

	private Integer@Server addServerEvent(ServerEvent@Server event) {
        try {
            serverQueue.put(event);
            return null@Server;
        }
        catch (InterruptedException@Server e) {
            // Ignore the interrupt and try again.
            return addServerEvent(event);
        }
	}

    public void run() {
        // TODO: Start clientDrivenLoop() and serverDrivenLoop() on separate
        // threads.
    }

    private void clientDrivenLoop() {
        ClientEvent@Client event = takeClientEvent();

        if (event.getType() == ClientEventType@Client.NICK) {
            ch_AB.<ClientEventType>select(ClientEventType@Client.NICK);

            ClientNickEvent@Client e = event.asClientNickEvent();
            String@Client nickname = e.getNickname();

            clientState.nickname = nickname;

            NickMessage@Server m = ch_AB.<NickMessage>com(
                new NickMessage@Client(nickname));
            serverState.nickname = m.getNickname();

            // TODO: Server: Add NICK events to all appropiate queues.
        }
        else {
            ch_AB.<ClientEventType>select(ClientEventType@Client.REGISTER);

            ClientRegisterEvent@Client e = event.asClientRegisterEvent();
            String@Client username = e.getUsername();
            String@Client realname = e.getRealname();

            clientState.username = username;
            clientState.realname = realname;

            RegisterMessage@Server m = ch_AB.<RegisterMessage>com(
                new RegisterMessage@Client(username, realname));
            serverState.username = m.getUsername();
            serverState.realname = m.getRealname();

            // TODO: Server: Add MOTD events to the appropriate queue.
        }

        clientDrivenLoop();
    }

    private void serverDrivenLoop() {
        ServerEvent@Server event = takeServerEvent();

        if (event.getType() == ServerEventType@Server.NICK) {
            ch_AB.<ServerEventType>select(ServerEventType@Server.NICK);

            ServerNickEvent@Server e = event.asServerNickEvent();
            serverState.nickname = e.getNickname();

            // TODO: Server: Reply with a numeric.
            // TODO: Client: Adjust local state (own or others' nicknames).
        }
        else {
            ch_AB.<ServerEventType>select(ServerEventType@Server.REGISTER);

            ServerRegisterEvent@Server e = event.asServerRegisterEvent();
            serverState.username = e.getUsername();
            serverState.realname = e.getRealname();

            // TODO: Server: Reply with a numeric.
            // TODO: Client: Adjust local state.
        }

        serverDrivenLoop();
    }
}
