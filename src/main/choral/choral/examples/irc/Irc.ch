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

    public Irc(SymChannel@(Client, Server)<Object> ch_AB,
               ClientState@Client clientState,
               ServerState@Server serverState) {
        this.ch_AB = ch_AB;
        this.clientState = clientState;
        this.clientQueue = new LinkedBlockingQueue@Client<ClientEvent>();
        this.serverState = serverState;
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
            addServerEvent(new ServerNickEvent@Server(sNickname));
        }
        else {
            ch_AB.<ClientEventType>select(ClientEventType@Client.USER);

            ClientUserEvent@Client e = event.asClientUserEvent();
            String@Client username = e.getUsername();
            String@Client realname = e.getRealname();

            UserMessage@Server m = ch_AB.<UserMessage>com(
                new UserMessage@Client(username, realname));

            clientState.setUsername(username);
            clientState.setRealname(realname);
            serverState.setUsername(m.getUsername());
            serverState.setRealname(m.getRealname());

            // TODO: Server: Add MOTD events to the appropriate queue.
        }

        clientDrivenLoop();
    }

    private void serverDrivenLoop() {
        ServerEvent@Server event = takeServerEvent();

        if (event.getType() == ServerEventType@Server.NICK) {
            ch_AB.<ServerEventType>select(ServerEventType@Server.NICK);

            ServerNickEvent@Server e = event.asServerNickEvent();
            String@Server nickname = e.getNickname();

            // TODO: Client: Adjust local state (own or others' nicknames).

            if (nickname == null@Server) {{{
                // ERR_NONICKNAMEGIVEN (431)
                ch_AB.<ServerEventType>select(ServerEventType@Server.ERR_NONICKNAMEGIVEN);
                ErrNoNicknameGivenMessage@Client err = ch_AB.<ErrNoNicknameGivenMessage>com(
                    new ErrNoNicknameGivenMessage@Server());

                clientState.revertNickname();
                clientState.getOut().println("Server: ERR_NONICKNAMEGIVNE"@Client);
            }}}
            else {
                if (!Util@Server.validNickname(nickname)) {{
                    // ERR_ERRONEUSNICKNAME (432)
                    ch_AB.<ServerEventType>select(ServerEventType@Server.ERR_ERRONEUSNICKNAME);

                    clientState.revertNickname();
                    clientState.getOut().println("Server: ERR_ERRONEUSNICKNAME"@Client);
                }}
                else {
                    if (serverState.nicknameInUse(nickname)) {
                        // ERR_NICKNAMEINUSE (433)
                        ch_AB.<ServerEventType>select(ServerEventType@Server.ERR_NICKNAMEINUSE);

                        clientState.revertNickname();
                        clientState.getOut().println("Server: ERR_NICKNAMEINUSE"@Client);
                    }
                    else {
                        // Success (TODO: Eventually remove this branch somehow.)
                            ch_AB.<ServerEventType>select(ServerEventType@Server.NICK_SUCCESS);

                        clientState.getOut().println("Server: Nickname changed successfully"@Client);
                    }
                }
            }
        }
        else {{{{
            ch_AB.<ServerEventType>select(ServerEventType@Server.USER);

            ServerUserEvent@Server e = event.asServerUserEvent();
            serverState.setUsername(e.getUsername());
            serverState.setRealname(e.getRealname());

            // TODO: Server: Reply with a numeric.
            // TODO: Client: Adjust local state.
        }}}}

        serverDrivenLoop();
    }
}
