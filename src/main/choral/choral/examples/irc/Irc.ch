package choral.examples.irc;

import choral.channels.SymChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Irc@(Client, Server) {
    private SymChannel@(Client, Server)<Message> ch_AB;

    private ClientState@Client clientState;
    private LinkedBlockingQueue@Client<ClientEvent> clientQueue;

    private ServerState@Server serverState;
    private LinkedBlockingQueue@Server<ServerEvent> serverQueue;
    private long@Server clientId;

    public Irc(SymChannel@(Client, Server)<Message> ch_AB,
               ClientState@Client clientState,
               ServerState@Server serverState) {
        this.ch_AB = ch_AB;

        this.clientState = clientState;
        this.clientQueue = new LinkedBlockingQueue@Client<ClientEvent>();

        this.serverState = serverState;
        this.serverQueue = new LinkedBlockingQueue@Server<ServerEvent>();
        this.clientId = serverState.newClient(serverQueue);
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
            PingMessage@Server m = ch_AB.<PingMessage>com(e.getMessage());

            serverState.getOut().println(m.toString());

            if (!serverState.isRegistered(clientId)) {{
                addServerEvent(new ServerForwardMessageEvent@Server(
                    new ErrNotRegisteredMessage@Server(
                        "*"@Server, "You must register first"@Server)));
            }}
            else {
                if (!m.hasEnoughParams()) {
                    addServerEvent(new ServerForwardMessageEvent@Server(
                        new ErrNotRegisteredMessage@Server(
                            serverState.getNickname(clientId),
                            "Need more parameters"@Server)));
                }
                else {
                    addServerEvent(new ServerPongEvent@Server(
                        new PongMessage@Server(
                            IrcServerLocalUtil@Server.HOSTNAME, m.getToken())));
                }
            }
        }}}}}}
        else {
            if (event.getType() == ClientEventType@Client.PONG) {{{{{
                ch_AB.<ClientEventType>select(ClientEventType@Client.PONG);

                ClientPongEvent@Client e = event.asClientPongEvent();
                PongMessage@Server m = ch_AB.<PongMessage>com(e.getMessage());

                serverState.getOut().println(m.toString());
            }}}}}
            else {
                if (event.getType() == ClientEventType@Client.NICK) {{{{
                    ch_AB.<ClientEventType>select(ClientEventType@Client.NICK);

                    ClientNickEvent@Client e = event.asClientNickEvent();
                    NickMessage@Client cMessage = e.getMessage();
                    NickMessage@Server sMessage = ch_AB.<NickMessage>com(cMessage);

                    clientState.setNickname(cMessage.getNickname());
                    IrcServerLocalUtil@Server.processNick(serverState, clientId, sMessage);
                }}}}
                else {
                    if (event.getType() == ClientEventType@Client.USER) {{{
                        ch_AB.<ClientEventType>select(ClientEventType@Client.USER);

                        ClientUserEvent@Client e = event.asClientUserEvent();
                        UserMessage@Client cMessage = e.getMessage();
                        UserMessage@Server sMessage = ch_AB.<UserMessage>com(cMessage);

                        clientState.setUsername(cMessage.getUsername());
                        clientState.setRealname(cMessage.getRealname());

                        if (!sMessage.hasEnoughParams()) {{{{
                            Message@Server m = new ErrNeedMoreParamsMessage@Server(
                                serverState.getNickname(clientId),
                                "Need more parameters"@Server);
                            serverState.addEvent(clientId, new ServerForwardMessageEvent@Server(m));
                        }}}}
                        else {
                            String@Server username = sMessage.getUsername();
                            String@Server realname = sMessage.getRealname();

                            if (serverState.isRegistered(clientId)) {{{
                                Message@Server m = new ErrAlreadyRegisteredMessage@Server(
                                    serverState.getNickname(clientId),
                                    "You cannot register again"@Server);
                                serverState.addEvent(clientId, new ServerForwardMessageEvent@Server(m));
                            }}}
                            else {
                                if (Util@Server.validUsername(username)) {
                                    serverState.setUsername(clientId, username);
                                    serverState.setRealname(clientId, realname);

                                    if (serverState.isRegistered(clientId) &&
                                        !serverState.isWelcomeDone(clientId)) {
                                        IrcServerLocalUtil@Server.processWelcome(serverState, clientId);
                                    }
                                }
                                else {{}}
                            }
                        }
                    }}}
                    else {
                        if (event.getType() == ClientEventType@Client.JOIN) {{
                            ch_AB.<ClientEventType>select(ClientEventType@Client.JOIN);

                            ClientJoinEvent@Client e = event.asClientJoinEvent();
                            JoinMessage@Server m = ch_AB.<JoinMessage>com(e.getMessage());

                            IrcServerLocalUtil@Server.processJoin(serverState, clientId, m);
                        }}
                        else {
                            if (event.getType() == ClientEventType@Client.PART) {
                                ch_AB.<ClientEventType>select(ClientEventType@Client.PART);

                                ClientPartEvent@Client e = event.asClientPartEvent();
                                PartMessage@Server m = ch_AB.<PartMessage>com(e.getMessage());

                                IrcServerLocalUtil@Server.processPart(serverState, clientId, m);
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
            PingMessage@Client m = ch_AB.<PingMessage>com(e.getMessage());

            clientState.getOut().println(m.toString());

            if (m.hasEnoughParams()) {
                Util@Client.<ClientEvent>put(clientQueue,
                    new ClientPongEvent@Client(new PongMessage@Client(m.getToken())));
            }
        }}}}}}}}
        else {
            if (event.getType() == ServerEventType@Server.PONG) {{{{{{{
                ch_AB.<ServerEventType>select(ServerEventType@Server.PONG);

                ServerPongEvent@Server e = event.asServerPongEvent();
                PongMessage@Client m = ch_AB.<PongMessage>com(e.getMessage());

                clientState.getOut().println(m.toString());
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

                        Source@Client source = m.getSource();

                        if (source != null@Client && m.hasEnoughParams()) {
                            List@Client<String> channels = m.getChannels();
                            String@Client nickname = source.getNickname();
                            // We expect just a single channel, so ignore the others, if any
                            String@Client channel = channels.get(0@Client);

                            if (nickname.equals(clientState.getNickname())) {
                                if (!clientState.inChannel(channel)) {
                                    clientState.joinChannel(channel);
                                }
                            }
                            else {
                                if (clientState.inChannel(channel)) {
                                    clientState.addMember(channel, nickname);
                                }
                            }
                        }
                        else {{{}}}
                    }}}}}
                    else {
                        if (event.getType() == ServerEventType@Server.PART) {{{{
                            ch_AB.<ServerEventType>select(ServerEventType@Server.PART);

                            ServerPartEvent@Server e = event.asServerPartEvent();
                            PartMessage@Client m = ch_AB.<PartMessage>com(e.getMessage());

                            clientState.getOut().println(m.toString());

                            Source@Client source = m.getSource();

                            if (source != null@Client && m.hasEnoughParams()) {
                                List@Client<String> channels = m.getChannels();
                                String@Client nickname = source.getNickname();
                                // NOTE: We expect just a single channel, so ignore the others, if any
                                String@Client channel = channels.get(0@Client);

                                if (nickname.equals(clientState.getNickname())) {
                                    if (clientState.inChannel(channel)) {
                                        clientState.partChannel(channel);
                                    }
                                }
                                else {
                                    if (clientState.inChannel(channel)) {
                                        clientState.removeMember(channel, nickname);
                                    }
                                }
                            }
                            else {{{}}}
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
}
