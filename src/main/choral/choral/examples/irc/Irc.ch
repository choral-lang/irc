package choral.examples.irc;

import choral.channels.SymChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Irc@(Client, Server) {
    private SymChannel@(Client, Server)<Object> ch_AB;

    private ClientState@Client clientState;
    private LinkedBlockingQueue@Client<ClientEvent> clientQueue;

    private ServerState@Server serverState;
    private LinkedBlockingQueue@Server<ServerEvent> serverQueue;
    private IrcServerLocal@Server serverLocal;

    public Irc(SymChannel@(Client, Server)<Object> ch_AB,
               ClientState@Client clientState,
               ServerState@Server serverState) {
        this.ch_AB = ch_AB;
        this.clientState = clientState;
        this.clientQueue = new LinkedBlockingQueue@Client<ClientEvent>();
        this.serverState = serverState;
        this.serverQueue = new LinkedBlockingQueue@Server<ServerEvent>();
        this.serverLocal = new IrcServerLocal@Server(serverState, serverQueue);
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

    /**
     * A loop driven by the client's event queue. The client initiates requests.
     */
    public void clientDrivenLoop() {
        ClientEvent@Client event = takeClientEvent();

        if (event.getType() == ClientEventType@Client.NICK) {
            ch_AB.<ClientEventType>select(ClientEventType@Client.NICK);

            ClientNickEvent@Client e = event.asClientNickEvent();
            String@Client cNickname = e.getNickname();
            String@Server sNickname;

            try {
                NickMessage@Server m = ch_AB.<NickMessage>com(
                    new NickMessage@Client(cNickname));
                sNickname = m.getNickname();
            }
            catch (NoNicknameGivenException@Server ex) {
                sNickname = null@Server;
            }

            clientState.setNickname(cNickname);
            serverLocal.addLocalEvent(
                new ServerLocalCheckNickEvent@Server(sNickname));
        }
        else {
            ch_AB.<ClientEventType>select(ClientEventType@Client.USER);

            ClientUserEvent@Client e = event.asClientUserEvent();
            String@Client cUsername = e.getUsername();
            String@Client cRealname = e.getRealname();
            String@Server sUsername;
            String@Server sRealname;

            try {
                UserMessage@Server m = ch_AB.<UserMessage>com(
                    new UserMessage@Client(cUsername, cRealname));
                sUsername = m.getUsername();
                sRealname = m.getRealname();
            }
            catch (NeedMoreParamsException@Server ex) {
                sUsername = null@Server;
                sRealname = null@Server;
            }

            clientState.setUsername(cUsername);
            clientState.setRealname(cRealname);
            serverLocal.addLocalEvent(
                new ServerLocalCheckUserEvent@Server(sUsername, sRealname));

            // TODO: Server: Add MOTD events to the appropriate queue.
        }

        clientDrivenLoop();
    }

    /**
     * A loop driven by the server's event queue. The server initiates requests.
     */
    public void serverDrivenLoop() {
        ServerEvent@Server event = takeServerEvent();

        if (event.getType() == ServerEventType@Server.NICK) {
            ch_AB.<ServerEventType>select(ServerEventType@Server.NICK);

            ServerNickEvent@Server e = event.asServerNickEvent();
            Message@Client m = ch_AB.<Message>com(e.getError());

            clientState.getOut().println(
                "Client: Error while changing nickname"@Client);
        }
        else {
            ch_AB.<ServerEventType>select(ServerEventType@Server.USER);

            ServerUserEvent@Server e = event.asServerUserEvent();
            Message@Client m = ch_AB.<Message>com(e.getError());

            clientState.getOut().println(
                "Client: Error while registering username"@Client);
        }

        serverDrivenLoop();
    }

    public void serverLocalLoop() {
        serverLocal.run();
    }
}
