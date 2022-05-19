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

            NickMessage@Server m = ch_AB.<NickMessage>com(
                new NickMessage@Client(cNickname));

            clientState.setNickname(cNickname);
            serverLocal.addLocalEvent(new ServerLocalCheckNickEvent@Server(m));
        }
        else {
            ch_AB.<ClientEventType>select(ClientEventType@Client.USER);

            ClientUserEvent@Client e = event.asClientUserEvent();
            String@Client cUsername = e.getUsername();
            String@Client cRealname = e.getRealname();

            UserMessage@Server m = ch_AB.<UserMessage>com(
                new UserMessage@Client(cUsername, cRealname));

            clientState.setUsername(cUsername);
            clientState.setRealname(cRealname);
            serverLocal.addLocalEvent(new ServerLocalCheckUserEvent@Server(m));
        }

        clientDrivenLoop();
    }

    public void clientRecvAndDisplay(String@Server command, String@Server param) {
        Message@Server ms = MessageBuilder@Server
            .build()
            .source(Source@Server.parse("irc.choral.net"@Server))
            .command(command)
            .param(serverState.getNickname())
            .param(param)
            .message();

        Message@Client mc = ch_AB.<Message>com(ms);
        clientState.getOut().println(mc.serialize());
    }

    /**
     * A loop driven by the server's event queue. The server initiates requests.
     */
    public void serverDrivenLoop() {
        ServerEvent@Server event = takeServerEvent();

        if (event.getType() == ServerEventType@Server.NICK_ERROR) {{{
            ch_AB.<ServerEventType>select(ServerEventType@Server.NICK_ERROR);

            ServerNickErrorEvent@Server e = event.asServerNickErrorEvent();
            Message@Client m = ch_AB.<Message>com(e.getReply());

            clientState.getOut().println(
                "Error while changing nickname: '"@Client +
                m.serialize() + "'"@Client);
        }}}
        else {
            if (event.getType() == ServerEventType@Server.USER_ERROR) {{
                ch_AB.<ServerEventType>select(ServerEventType@Server.USER_ERROR);

                ServerUserErrorEvent@Server e = event.asServerUserErrorEvent();
                Message@Client m = ch_AB.<Message>com(e.getReply());

                clientState.getOut().println(
                    "Error while registering username: '"@Client +
                    m.serialize() + "'"@Client);
            }}
            else {
                if (event.getType() == ServerEventType@Server.REGISTRATION_COMPLETE) {
                    ch_AB.<ServerEventType>select(ServerEventType@Server.REGISTRATION_COMPLETE);

                    clientRecvAndDisplay(Message@Server.RPL_WELCOME, "Welcome to ChoralNet!"@Server);
                    clientRecvAndDisplay(Message@Server.RPL_YOURHOST, "Your host is irc.choral.net"@Server);
                    clientRecvAndDisplay(Message@Server.RPL_CREATED, "The server was created at IMADA"@Server);
                    clientRecvAndDisplay(Message@Server.RPL_MYINFO, "I'm running ChoralIRC 0.0.1"@Server);
                    clientRecvAndDisplay(Message@Server.RPL_ISUPPORT, "NICKLEN=32"@Server);
                    clientRecvAndDisplay(Message@Server.RPL_UMODEIS, "+i"@Server);

                    clientRecvAndDisplay(Message@Server.RPL_LUSERCLIENT, "There's only me and you here"@Server);
                    // clientRecvAndDisplay(Message@Server.RPL_LUSEROP, ""@Server);
                    // clientRecvAndDisplay(Message@Server.RPL_LUSERUNKNOWN, ""@Server);
                    // clientRecvAndDisplay(Message@Server.RPL_LUSERCHANNELS, ""@Server);
                    clientRecvAndDisplay(Message@Server.RPL_LUSERME, "I have exactly one user---you"@Server);
                    // clientRecvAndDisplay(Message@Server.RPL_LOCALUSERS, ""@Server);
                    // clientRecvAndDisplay(Message@Server.RPL_GLOBALUSERS, ""@Server);

                    clientRecvAndDisplay(Message@Server.RPL_MOTDSTART, "ChoralNet Message of the Day"@Server);
                    clientRecvAndDisplay(Message@Server.RPL_MOTD, "Hopefully you're having a nice day!"@Server);
                    clientRecvAndDisplay(Message@Server.RPL_MOTD, "Come find us in the office working..."@Server);
                    clientRecvAndDisplay(Message@Server.RPL_MOTD, "...or having a choco break in the lunchroom!"@Server);
                    clientRecvAndDisplay(Message@Server.RPL_ENDOFMOTD, "End of /MOTD command"@Server);
                }
                else {
                    ch_AB.<ServerEventType>select(ServerEventType@Server.UNKNOWN);
                }
            }
        }

        serverDrivenLoop();
    }

    public void serverLocalLoop() {
        serverLocal.run();
    }
}
