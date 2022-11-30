package choral.examples.irc;

import java.util.List;

public class IrcClientHandler@(Client, Server)
        implements EventHandler@(Client, Server)<Message> {
    private EventQueue@Server<Message> serverQueue;
    private IrcChannel@(Client, Server) ch_AB;

    private ClientState@Client clientState;

    private ServerState@Server serverState;
    private long@Server clientId;

    public IrcClientHandler(EventQueue@Server<Message> serverQueue,
                            IrcChannel@(Client, Server) ch_AB,
                            ClientState@Client clientState,
                            ServerState@Server serverState,
                            long@Server clientId) {
        this.serverQueue = serverQueue;
        this.ch_AB = ch_AB;

        this.clientState = clientState;

        this.serverState = serverState;
        this.clientId = clientId;
    }

    /**
     * One step of the event loop driven by the client's message queue. Only the
     * client initiates requests.
     */
    public void on(Message@Client msg) {
        Command@Client cmd = Util@Client.commandFromString(msg.getCommand());

        Util@Client.check(cmd != null@Client,
                          "Expected a known message"@Client);

        switch (cmd) {
            case PING -> {
                PingMessage@Server ping = ch_AB.<PingMessage>sselect(
                    Util@Client.<PingMessage>as(msg));

                serverState.getOut().println(ping.toString());

                if (!serverState.isRegistered(clientId)) {{
                    serverQueue.enqueue(ServerUtil@Server.forwardNumeric(
                        Command@Server.ERR_NOTREGISTERED, "*"@Server,
                        "You must register first"@Server));
                }}
                else {
                    if (!ping.hasEnoughParams()) {
                        serverQueue.enqueue(ServerUtil@Server.forwardNumeric(
                            Command@Server.ERR_NEEDMOREPARAMS,
                            serverState.getNickname(clientId),
                            "Need more parameters"@Server));
                    }
                    else {
                        serverQueue.enqueue(
                            ServerUtil@Server.<PongMessage>withSource(
                                new PongMessage@Server(
                                    ServerUtil@Server.HOSTNAME,
                                    ping.getToken()),
                                Source@Server.parse(
                                    ServerUtil@Server.HOSTNAME)));
                    }
                }
            }

            case PONG -> {
                PongMessage@Server pong = ch_AB.<PongMessage>sselect(
                    Util@Client.<PongMessage>as(msg));

                serverState.getOut().println(pong.toString());
            }

            case NICK -> {
                NickMessage@Client cNick = Util@Client.<NickMessage>as(msg);
                NickMessage@Server sNick = ch_AB.<NickMessage>sselect(cNick);

                if (!clientState.isRegistered()) {
                    clientState.setNickname(cNick.getNickname());
                }

                ServerUtil@Server.processNick(serverState, clientId, sNick);
            }

            case USER -> {
                UserMessage@Client cUser = Util@Client.<UserMessage>as(msg);
                UserMessage@Server sUser = ch_AB.<UserMessage>sselect(cUser);

                if (!clientState.isRegistered()) {
                    clientState.setUsername(cUser.getUsername());
                    clientState.setRealname(cUser.getRealname());
                }

                if (serverState.isRegistered(clientId)) {{{{
                    serverQueue.enqueue(ServerUtil@Server.forwardNumeric(
                        Command@Server.ERR_ALREADYREGISTERED,
                        serverState.getNickname(clientId),
                        "You cannot register again"@Server));
                }}}}
                else {
                    if (!sUser.hasEnoughParams()) {{{
                        serverQueue.enqueue(ServerUtil@Server.forwardNumeric(
                            Command@Server.ERR_NEEDMOREPARAMS,
                            "*"@Server, "Need more parameters"@Server));
                    }}}
                    else {
                        String@Server username = sUser.getUsername();
                        String@Server realname = sUser.getRealname();

                        if (Util@Server.validUsername(username)) {
                            serverState.setUsername(clientId, username);
                            serverState.setRealname(clientId, realname);

                            if (serverState.canRegister(clientId) &&
                                !serverState.isRegistered(clientId)) {
                                ServerUtil@Server.processWelcome(
                                    serverState, clientId);
                            }
                        }
                        else {{}}
                    }
                }
            }

            case JOIN -> {
                JoinMessage@Server join = ch_AB.<JoinMessage>sselect(
                    Util@Client.<JoinMessage>as(msg));

                ServerUtil@Server.processJoin(serverState, clientId, join);
            }

            case PART -> {
                PartMessage@Server part = ch_AB.<PartMessage>sselect(
                    Util@Client.<PartMessage>as(msg));

                ServerUtil@Server.processPart(serverState, clientId, part);
            }

            case PRIVMSG -> {
                PrivmsgMessage@Server privmsg = ch_AB.<PrivmsgMessage>sselect(
                    Util@Client.<PrivmsgMessage>as(msg));

                ServerUtil@Server.processPrivmsg(
                    serverState, clientId, privmsg);
            }

            case QUIT -> {
                QuitMessage@Server quit = ch_AB.<QuitMessage>sselect(
                    Util@Client.<QuitMessage>as(msg));

                ServerUtil@Server.processQuit(serverState, clientId, quit);
            }
        }
    }
}
