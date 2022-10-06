package choral.examples.irc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Irc@(Client, Server) {
    private IrcChannel@(Client, Server) ch_AB;

    private ClientState@Client clientState;
    private LinkedBlockingQueue@Client<Message> clientQueue;

    private ServerState@Server serverState;
    private long@Server clientId;
    private LinkedBlockingQueue@Server<Message> serverQueue;

    public Irc(IrcChannel@(Client, Server) ch_AB,
               ClientState@Client clientState,
               ServerState@Server serverState) {
        this.ch_AB = ch_AB;

        this.clientState = clientState;
        clientQueue = clientState.getQueue();

        this.serverState = serverState;
        clientId = serverState.newClient();
        serverQueue = serverState.getQueue(clientId);
    }

    public ClientState@Client getClientState() {
        return clientState;
    }

    public ServerState@Server getServerState() {
        return serverState;
    }

    public long@Server getClientId() {
        return clientId;
    }

    public void addClientMessage(Message@Client message) {
        Util@Client.<Message>put(clientQueue, message);
    }

    public void addServerMessage(Message@Server message) {
        Util@Server.<Message>put(serverQueue, message);
    }

    /**
     * One step of the loop driven by the client's message queue. Only the
     * client initiates requests.
     */
    public void clientProcessOne(Message@Client msg) {
        Command@Client cmd = Util@Client.commandFromString(msg.getCommand());

        Util@Client.check(cmd != null@Client,
                          "Expected a known message"@Client);

        switch (cmd) {
            case PING -> {
                PingMessage@Server ping = ch_AB.<PingMessage>sselect(
                    Util@Client.<PingMessage>as(msg));

                serverState.getOut().println(ping.toString());

                if (!serverState.isRegistered(clientId)) {{
                    addServerMessage(ServerUtil@Server.forwardNumeric(
                        Command@Server.ERR_NOTREGISTERED, "*"@Server,
                        "You must register first"@Server));
                }}
                else {
                    if (!ping.hasEnoughParams()) {
                        addServerMessage(ServerUtil@Server.forwardNumeric(
                            Command@Server.ERR_NEEDMOREPARAMS,
                            serverState.getNickname(clientId),
                            "Need more parameters"@Server));
                    }
                    else {
                        addServerMessage(
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
                    serverState.addMessage(clientId,
                        ServerUtil@Server.forwardNumeric(
                            Command@Server.ERR_ALREADYREGISTERED,
                            serverState.getNickname(clientId),
                            "You cannot register again"@Server));
                }}}}
                else {
                    if (!sUser.hasEnoughParams()) {{{
                        serverState.addMessage(clientId,
                            ServerUtil@Server.forwardNumeric(
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
                                ServerUtil@Server.processWelcome(serverState, clientId);
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

                ServerUtil@Server.processPrivmsg(serverState, clientId, privmsg);
            }

            case QUIT -> {
                QuitMessage@Server quit = ch_AB.<QuitMessage>sselect(
                    Util@Client.<QuitMessage>as(msg));

                ServerUtil@Server.processQuit(serverState, clientId, quit);
            }
        }
    }

    /**
     * One step of the loop driven by the server's message queue. Only the
     * server initiates requests.
     */
    public void serverProcessOne(Message@Server msg) {
        Command@Server cmd = Util@Server.commandFromString(msg.getCommand());

        Util@Server.check(cmd != null@Server,
                          "Expected a known message"@Server);

        switch (cmd) {
            case PING -> {
                PingMessage@Client ping = ch_AB.<PingMessage>sselect(
                    Util@Server.<PingMessage>as(msg));

                clientState.getOut().println(ping.toString());

                if (ping.hasEnoughParams()) {
                    Util@Client.<Message>put(clientQueue,
                        new PongMessage@Client(ping.getToken()));
                }
            }

            case PONG -> {
                PongMessage@Client pong = ch_AB.<PongMessage>sselect(
                    Util@Server.<PongMessage>as(msg));

                clientState.getOut().println(pong.toString());
            }

            case NICK -> {
                NickMessage@Client nick = ch_AB.<NickMessage>sselect(
                    Util@Server.<NickMessage>as(msg));

                clientState.getOut().println(nick.toString());

                if (nick.hasEnoughParams()) {
                    Source@Client source = nick.getSource();

                    if (source != null@Client) {
                        String@Client current = clientState.getNickname();
                        String@Client from = source.getNickname();
                        String@Client to = nick.getNickname();

                        if (current != null@Client && current.equals(from)) {
                            clientState.setNickname(to);
                        }
                        else {
                            clientState.renameMember(from, to);
                        }
                    }
                    else {{}}
                }
                else {{{}}}
            }

            case JOIN -> {
                JoinMessage@Client join = ch_AB.<JoinMessage>sselect(
                    Util@Server.<JoinMessage>as(msg));

                clientState.getOut().println(join.toString());

                Source@Client source = join.getSource();

                if (source != null@Client && join.hasEnoughParams()) {
                    List@Client<String> channels = join.getChannels();
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
            }

            case PART -> {
                PartMessage@Client part = ch_AB.<PartMessage>sselect(
                    Util@Server.<PartMessage>as(msg));

                clientState.getOut().println(part.toString());

                Source@Client source = part.getSource();

                if (source != null@Client && part.hasEnoughParams()) {
                    List@Client<String> channels = part.getChannels();
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
            }

            case PRIVMSG -> {
                PrivmsgMessage@Client privmsg = ch_AB.<PrivmsgMessage>sselect(
                    Util@Server.<PrivmsgMessage>as(msg));

                clientState.getOut().println(privmsg.toString());
            }

            case RPL_WELCOME -> {
                RplWelcomeMessage@Client welcome = ch_AB.<RplWelcomeMessage>sselect(
                    Util@Server.<RplWelcomeMessage>as(msg));

                clientState.getOut().println(welcome.toString());

                if (welcome.hasEnoughParams()) {
                    clientState.setNickname(welcome.getNickname());
                    clientState.setRegistered();
                }
            }

            case RPL_NAMREPLY -> {
                RplNamReplyMessage@Client namReply = ch_AB.<RplNamReplyMessage>sselect(
                    Util@Server.<RplNamReplyMessage>as(msg));

                clientState.getOut().println(namReply.toString());

                String@Client channel = namReply.getChannel();

                if (namReply.hasEnoughParams() && clientState.inChannel(channel)) {
                    clientState.addMembers(channel, namReply.getNicknames());
                }
            }

            case QUIT -> {
                QuitMessage@Client quit = ch_AB.<QuitMessage>sselect(
                    Util@Server.<QuitMessage>as(msg));

                Source@Client source = quit.getSource();

                if (source != null@Client) {
                    String@Client nickname = source.getNickname();

                    if (nickname != clientState.getNickname()) {
                        String@Client info = "Client '"@Client + nickname
                            + "' quit"@Client;

                        if (quit.hasEnoughParams()) {
                            info = info + " ("@Client + quit.getReason() +
                                ")"@Client;
                        }

                        clientState.getOut().println(info);
                        clientState.removeMember(nickname);
                    }
                    else {{}}
                }
                else {{{}}}
            }

            case ERROR -> {
                ErrorMessage@Client error = ch_AB.<ErrorMessage>sselect(
                    Util@Server.<ErrorMessage>as(msg));

                String@Client info = "Disconnected"@Client;

                if (error.hasEnoughParams()) {
                    info = info + " ("@Client + error.getReason() + ")"@Client;
                }

                clientState.getOut().println(info);

                // Set the gracefulQuit flags to communicate a graceful close.
                serverState.setGracefulQuit(clientId);
                clientState.setGracefulQuit();
                ch_AB.close();
            }

            // NOTE: Any message that falls under ForwardMessage
            default -> {
                // We can use whichever ForwardMessage for the selection
                ForwardMessage@Client forward = ch_AB.<ForwardMessage>sselect(
                    Util@Server.<ForwardMessage>as(msg));

                Util@Server.check(ForwardMessage@Server.COMMANDS.contains(cmd),
                                  "Expected a ForwardMessage"@Server);

                clientState.getOut().println(forward.toString());
            }
        }
    }
}
