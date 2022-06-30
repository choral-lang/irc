package choral.examples.irc;

import choral.channels.SymChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Irc@(Client, Server) {
    private SymChannel@(Client, Server)<Message> ch_AB;

    private ClientState@Client clientState;
    private LinkedBlockingQueue@Client<ClientEvent> clientQueue;
    private IrcClientLocal@Client clientLocal;

    private ServerState@Server serverState;
    private LinkedBlockingQueue@Server<ServerEvent> serverQueue;
    private long@Server clientId;
    private IrcServerLocal@Server serverLocal;

    public Irc(SymChannel@(Client, Server)<Message> ch_AB,
               ClientState@Client clientState,
               ServerState@Server serverState) {
        this.ch_AB = ch_AB;

        this.clientState = clientState;
        this.clientQueue = new LinkedBlockingQueue@Client<ClientEvent>();
        this.clientLocal = new IrcClientLocal@Client(clientState, clientQueue);

        this.serverState = serverState;
        this.serverQueue = new LinkedBlockingQueue@Server<ServerEvent>();
        this.clientId = serverState.newClient(serverQueue);
        this.serverLocal = new IrcServerLocal@Server(serverState, clientId);
    }

    public void addClientEvent(ClientEvent@Client event) {
        Util@Client.<ClientEvent>put(clientQueue, event);
    }

    public void addServerEvent(ServerEvent@Server event) {
        Util@Server.<ServerEvent>put(serverQueue, event);
    }

    /**
     * A loop driven by the client's event queue. The client initiates requests.
     */
    public void clientDrivenLoop() {
        ClientEvent@Client event = Util@Client.<ClientEvent>take(clientQueue);

        if (event.getType() == ClientEventType@Client.PING) {{{{{{
            ch_AB.<ClientEventType>select(ClientEventType@Client.PING);

            ClientPingEvent@Client e = event.asClientPingEvent();
            String@Client token = e.getToken();

            PingMessage@Server m = ch_AB.<PingMessage>com(
                new PingMessage@Client(token));

            addServerEvent(new ServerPongEvent@Server(m));
        }}}}}}
        else {
            if (event.getType() == ClientEventType@Client.PONG) {{{{{
                ch_AB.<ClientEventType>select(ClientEventType@Client.PONG);

                ClientPongEvent@Client e = event.asClientPongEvent();
                String@Client token = e.getToken();

                PongMessage@Server m = ch_AB.<PongMessage>com(
                    new PongMessage@Client(token));

                serverState.getOut().println(m.toString());
            }}}}}
            else {
                if (event.getType() == ClientEventType@Client.NICK) {{{{
                    ch_AB.<ClientEventType>select(ClientEventType@Client.NICK);

                    ClientNickEvent@Client e = event.asClientNickEvent();
                    String@Client cNickname = e.getNickname();

                    NickMessage@Server m = ch_AB.<NickMessage>com(
                        new NickMessage@Client(cNickname));

                    clientState.setNickname(cNickname);
                    serverLocal.addLocalEvent(new ServerLocalCheckNickEvent@Server(m));
                }}}}
                else {
                    if (event.getType() == ClientEventType@Client.USER) {{{
                        ch_AB.<ClientEventType>select(ClientEventType@Client.USER);

                        ClientUserEvent@Client e = event.asClientUserEvent();
                        String@Client cUsername = e.getUsername();
                        String@Client cRealname = e.getRealname();

                        UserMessage@Server m = ch_AB.<UserMessage>com(
                            new UserMessage@Client(cUsername, cRealname));

                        clientState.setUsername(cUsername);
                        clientState.setRealname(cRealname);
                        serverLocal.addLocalEvent(new ServerLocalCheckUserEvent@Server(m));
                    }}}
                    else {
                        if (event.getType() == ClientEventType@Client.JOIN) {{
                            ch_AB.<ClientEventType>select(ClientEventType@Client.JOIN);

                            ClientJoinEvent@Client e = event.asClientJoinEvent();
                            List@Client<String> channels = e.getChannels();

                            JoinMessage@Server m = ch_AB.<JoinMessage>com(
                                new JoinMessage@Client(channels));

                            serverLocal.addLocalEvent(new ServerLocalJoinEvent@Server(m));
                        }}
                        else {
                            if (event.getType() == ClientEventType@Client.PART) {
                                ch_AB.<ClientEventType>select(ClientEventType@Client.PART);

                                ClientPartEvent@Client e = event.asClientPartEvent();
                                List@Client<String> channels = e.getChannels();

                                PartMessage@Server m = ch_AB.<PartMessage>com(
                                    new PartMessage@Client(channels));

                                serverLocal.addLocalEvent(new ServerLocalPartEvent@Server(m));
                            }
                            else {
                                ch_AB.<ClientEventType>select(ClientEventType@Client.PRIVMSG);

                                ClientPrivmsgEvent@Client e = event.asClientPrivmsgEvent();
                                PrivmsgMessage@Server m = ch_AB.<PrivmsgMessage>com(e.getMessage());

                                IrcServerLocalUtil@Server.processPrivmsg(serverState, clientId, m);
                            }
                        }
                    }
                }
            }
        }

        clientDrivenLoop();
    }

    /**
     * A loop driven by the server's event queue. The server initiates requests.
     */
    public void serverDrivenLoop() {
        ServerEvent@Server event = Util@Server.<ServerEvent>take(serverQueue);

        if (event.getType() == ServerEventType@Server.PING) {{{{{{{{
            ch_AB.<ServerEventType>select(ServerEventType@Server.PING);

            ServerPingEvent@Server e = event.asServerPingEvent();
            String@Server sToken = e.getToken();

            PingMessage@Client m = ch_AB.<PingMessage>com(
                new PingMessage@Server(sToken));

            clientLocal.addLocalEvent(new ClientLocalPongEvent@Client(m));
        }}}}}}}}
        else {
            if (event.getType() == ServerEventType@Server.PONG) {{{{{{{
                // TODO: Why is this necessary?
                ch_AB.<ServerEventType>select(ServerEventType@Server.PONG);

                if (!serverState.isRegistered(clientId)) {{
                    ch_AB.<Command>select(Command@Server.ERR_NOTREGISTERED);

                    Message@Client m = ch_AB.<Message>com(new ErrNotRegisteredMessage@Server(
                        "*"@Server, "You must register first!"@Server));

                    clientState.getOut().println("Error: "@Client + m.toString());
                }}
                else {
                    ServerPongEvent@Server e = event.asServerPongEvent();
                    PingMessage@Server ping = e.getMessage();

                    if (!ping.hasEnoughParams()) {
                        ch_AB.<Command>select(Command@Server.ERR_NEEDMOREPARAMS);

                        Message@Client m = ch_AB.<Message>com(new ErrNeedMoreParamsMessage@Server(
                            serverState.getNickname(clientId),
                            "Need at least 1 parameter!"@Server));

                        clientState.getOut().println("Error: "@Client + m.toString());
                    }
                    else {
                        ch_AB.<Command>select(Command@Server.PONG);

                        PongMessage@Client m = ch_AB.<PongMessage>com(new PongMessage@Server(
                            "irc.choral.net"@Server, ping.getToken()));

                        clientState.getOut().println("PONG"@Client);
                    }
                }
            }}}}}}}
            else {
                if (event.getType() == ServerEventType@Server.NICK) {{{{{{
                    ch_AB.<ServerEventType>select(ServerEventType@Server.NICK);

                    ServerNickEvent@Server e = event.asServerNickEvent();
                    NickMessage@Client m = ch_AB.<NickMessage>com(e.getMessage());

                    clientState.getOut().println(m.toString());

                    if (m.hasEnoughParams()) {
                        IrcClientLocalUtil@Client.processNick(clientState, m);
                    }
                }}}}}}
                else {
                    if (event.getType() == ServerEventType@Server.JOIN) {{{{{
                        ch_AB.<ServerEventType>select(ServerEventType@Server.JOIN);

                        ServerJoinEvent@Server e = event.asServerJoinEvent();
                        JoinMessage@Client m = ch_AB.<JoinMessage>com(e.getMessage());

                        clientState.getOut().println(m.toString());

                        clientLocal.addLocalEvent(new ClientLocalJoinEvent@Client(m));
                    }}}}}
                    else {
                        if (event.getType() == ServerEventType@Server.PART) {{{{
                            ch_AB.<ServerEventType>select(ServerEventType@Server.PART);

                            ServerPartEvent@Server e = event.asServerPartEvent();
                            PartMessage@Client m = ch_AB.<PartMessage>com(e.getMessage());

                            clientState.getOut().println(m.toString());

                            clientLocal.addLocalEvent(new ClientLocalPartEvent@Client(m));
                        }}}}
                        else {
                            if (event.getType() == ServerEventType@Server.PRIVMSG) {{{
                                ch_AB.<ServerEventType>select(ServerEventType@Server.PRIVMSG);

                                ServerPrivmsgEvent@Server e = event.asServerPrivmsgEvent();
                                PrivmsgMessage@Client m = ch_AB.<PrivmsgMessage>com(e.getMessage());

                                clientState.getOut().println(m.toString());
                            }}}
                            else {
                                if (event.getType() == ServerEventType@Server.RPL_WELCOME) {{
                                    ch_AB.<ServerEventType>select(ServerEventType@Server.RPL_WELCOME);

                                    ServerRplWelcomeEvent@Server e = event.asServerRplWelcomeEvent();
                                    RplWelcomeMessage@Client m = ch_AB.<RplWelcomeMessage>com(e.getMessage());

                                    clientState.getOut().println(m.toString());

                                    if (m.hasEnoughParams()) {
                                        clientState.setNickname(m.getNickname());
                                    }
                                }}
                                else {
                                    if (event.getType() == ServerEventType@Server.RPL_NAMREPLY) {
                                        ch_AB.<ServerEventType>select(ServerEventType@Server.RPL_NAMREPLY);

                                        ServerRplNamReplyEvent@Server e = event.asServerRplNamReplyEvent();
                                        RplNamReplyMessage@Client m = ch_AB.<RplNamReplyMessage>com(e.getMessage());

                                        clientState.getOut().println(m.toString());

                                        String@Client channel = m.getChannel();

                                        if (m.hasEnoughParams() && clientState.inChannel(channel)) {
                                            IrcClientLocalUtil@Client.addMembers(
                                                clientState, channel, m.getNicknames());
                                        }
                                    }
                                    else {
                                        ch_AB.<ServerEventType>select(ServerEventType@Server.FORWARD_MESSAGE);

                                        ServerForwardMessageEvent@Server e = event.asServerForwardMessageEvent();
                                        Message@Client m = ch_AB.<Message>com(e.getMessage());

                                        clientState.getOut().println(m.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        serverDrivenLoop();
    }

    public void clientLocalLoop() {
        clientLocal.run();
    }

    public void serverLocalLoop() {
        serverLocal.run();
    }
}
