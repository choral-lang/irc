package choral.examples.irc;

import choral.channels.DiChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Irc@(Client, Server) {
    private ClientState@Client clientState;
    private LinkedBlockingQueue@Client<Message> clientQueue;

    private ServerState@Server serverState;
    private LinkedBlockingQueue@Server<Message> serverQueue;
    private long@Server clientId;

    public Irc(ClientState@Client clientState,
               ServerState@Server serverState) {
        this.clientState = clientState;
        this.clientQueue = new LinkedBlockingQueue@Client<Message>();

        this.serverState = serverState;
        this.serverQueue = new LinkedBlockingQueue@Server<Message>();
        this.clientId = serverState.newClient(serverQueue);
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
    public void clientProcessOne(DiChannel@(Client, Server)<Message> ch) {
        Message@Client msg = Util@Client.<Message>take(clientQueue);
        Command@Client cmd = Util@Client.commandFromString(msg.getCommand());

        // NOTE: We assume cmd != null
        switch (cmd) {
            case PING -> {
                ch.<Command>select(Command@Client.PING);
                PingMessage@Server ping = ch.<PingMessage>com(
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
                        addServerMessage(new PongMessage@Server(
                            ServerUtil@Server.HOSTNAME, ping.getToken()));
                    }
                }
            }

            case PONG -> {
                ch.<Command>select(Command@Client.PONG);
                PongMessage@Server pong = ch.<PongMessage>com(
                    Util@Client.<PongMessage>as(msg));

                serverState.getOut().println(pong.toString());
            }

            case NICK -> {
                ch.<Command>select(Command@Client.NICK);
                NickMessage@Client cNick = Util@Client.<NickMessage>as(msg);
                NickMessage@Server sNick = ch.<NickMessage>com(cNick);

                if (!clientState.isRegistered()) {
                    clientState.setNickname(cNick.getNickname());
                }

                ServerUtil@Server.processNick(serverState, clientId, sNick);
            }

            case USER -> {
                ch.<Command>select(Command@Client.USER);
                UserMessage@Client cUser = Util@Client.<UserMessage>as(msg);
                UserMessage@Server sUser = ch.<UserMessage>com(cUser);

                if (!clientState.isRegistered()) {
                    clientState.setUsername(cUser.getUsername());
                    clientState.setRealname(cUser.getRealname());
                }

                if (!sUser.hasEnoughParams()) {{{{
                    serverState.addMessage(clientId,
                        ServerUtil@Server.forwardNumeric(
                            Command@Server.ERR_NEEDMOREPARAMS,
                            serverState.getNickname(clientId),
                            "Need more parameters"@Server));
                }}}}
                else {
                    String@Server username = sUser.getUsername();
                    String@Server realname = sUser.getRealname();

                    if (serverState.isRegistered(clientId)) {{{
                        serverState.addMessage(clientId,
                            ServerUtil@Server.forwardNumeric(
                                Command@Server.ERR_ALREADYREGISTERED,
                                serverState.getNickname(clientId),
                                "You cannot register again"@Server));
                    }}}
                    else {
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
                ch.<Command>select(Command@Client.JOIN);
                JoinMessage@Server join = ch.<JoinMessage>com(
                    Util@Client.<JoinMessage>as(msg));

                ServerUtil@Server.processJoin(serverState, clientId, join);
            }

            case PART -> {
                ch.<Command>select(Command@Client.PART);
                PartMessage@Server part = ch.<PartMessage>com(
                    Util@Client.<PartMessage>as(msg));

                ServerUtil@Server.processPart(serverState, clientId, part);
            }

            case PRIVMSG -> {
                ch.<Command>select(Command@Client.PRIVMSG);
                PrivmsgMessage@Server privmsg = ch.<PrivmsgMessage>com(
                    Util@Client.<PrivmsgMessage>as(msg));

                ServerUtil@Server.processPrivmsg(serverState, clientId, privmsg);
            }
        }
    }

    /**
     * One step of the loop driven by the server's message queue. Only the
     * server initiates requests.
     */
    public void serverProcessOne(DiChannel@(Server, Client)<Message> ch) {
        Message@Server msg = Util@Server.<Message>take(serverQueue);
        Command@Server cmd = Util@Server.commandFromString(msg.getCommand());

        // NOTE: We assume cmd != null
        switch (cmd) {
            case PING -> {
                ch.<Command>select(Command@Server.PING);
                PingMessage@Client ping = ch.<PingMessage>com(
                    Util@Server.<PingMessage>as(msg));

                clientState.getOut().println(ping.toString());

                if (ping.hasEnoughParams()) {
                    Util@Client.<Message>put(clientQueue,
                        new PongMessage@Client(ping.getToken()));
                }
            }

            case PONG -> {
                ch.<Command>select(Command@Server.PONG);
                PongMessage@Client pong = ch.<PongMessage>com(
                    Util@Server.<PongMessage>as(msg));

                clientState.getOut().println(pong.toString());
            }

            case NICK -> {
                ch.<Command>select(Command@Server.NICK);
                NickMessage@Client nick = ch.<NickMessage>com(
                    Util@Server.<NickMessage>as(msg));

                clientState.getOut().println(nick.toString());

                if (nick.hasEnoughParams()) {
                    ClientUtil@Client.processNick(clientState, nick);
                }
            }

            case JOIN -> {
                ch.<Command>select(Command@Server.JOIN);
                JoinMessage@Client join = ch.<JoinMessage>com(
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
                ch.<Command>select(Command@Server.PART);
                PartMessage@Client part = ch.<PartMessage>com(
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
                ch.<Command>select(Command@Server.PRIVMSG);
                PrivmsgMessage@Client privmsg = ch.<PrivmsgMessage>com(
                    Util@Server.<PrivmsgMessage>as(msg));

                clientState.getOut().println(privmsg.toString());
            }

            case RPL_WELCOME -> {
                ch.<Command>select(Command@Server.RPL_WELCOME);
                RplWelcomeMessage@Client welcome = ch.<RplWelcomeMessage>com(
                    Util@Server.<RplWelcomeMessage>as(msg));

                clientState.getOut().println(welcome.toString());

                if (welcome.hasEnoughParams()) {
                    clientState.setNickname(welcome.getNickname());
                    clientState.setRegistered();
                }
            }

            case RPL_NAMREPLY -> {
                ch.<Command>select(Command@Server.RPL_NAMREPLY);
                RplNamReplyMessage@Client namReply = ch.<RplNamReplyMessage>com(
                    Util@Server.<RplNamReplyMessage>as(msg));

                clientState.getOut().println(namReply.toString());

                String@Client channel = namReply.getChannel();

                if (namReply.hasEnoughParams() && clientState.inChannel(channel)) {
                    ClientUtil@Client.addMembers(
                        clientState, channel, namReply.getNicknames());
                }
            }

            default -> {
                // Any message that falls under ForwardMessage can be
                // used for the selection.
                ch.<Command>select(Command@Server.ERR_NEEDMOREPARAMS);
                ForwardMessage@Client forward = ch.<ForwardMessage>com(
                    Util@Server.<ForwardMessage>as(msg));

                clientState.getOut().println(forward.toString());
            }
        }
    }
}
