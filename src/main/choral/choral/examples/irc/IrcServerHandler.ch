package choral.examples.irc;

import java.util.List;

public class IrcServerHandler@(Client, Server)
        implements EventHandler@(Server, Client)<Message> {
    private EventQueue@Client<Message> clientQueue;
    private IrcChannel@(Client, Server) ch_AB;

    private ClientState@Client clientState;

    private ServerState@Server serverState;
    private long@Server clientId;

    public IrcServerHandler(EventQueue@Client<Message> clientQueue,
                            IrcChannel@(Client, Server) ch_AB,
                            ClientState@Client clientState,
                            ServerState@Server serverState,
                            long@Server clientId) {
        this.clientQueue = clientQueue;
        this.ch_AB = ch_AB;

        this.clientState = clientState;

        this.serverState = serverState;
        this.clientId = clientId;
    }

    /**
     * One step of the event loop driven by the server's message queue. Only the
     * server initiates requests.
     */
    public void on(Message@Server msg) {
        Command@Server cmd = Util@Server.commandFromString(msg.getCommand());
        Util@Server.check(cmd != null@Server, "Expected a known message"@Server);

        ServerUtil@Server.printSend(serverState, clientId, msg);

        switch (cmd) {
            case PING -> {
                PingMessage@Client ping = ch_AB.<PingMessage>tselect(
                    Util@Server.<PingMessage>as(msg));

                clientState.getOut().println(ping.toString());

                if (ping.hasEnoughParams()) {
                    clientQueue.enqueue(new PongMessage@Client(ping.getToken()));
                }
            }

            case PONG -> {
                PongMessage@Client pong = ch_AB.<PongMessage>tselect(
                    Util@Server.<PongMessage>as(msg));

                clientState.getOut().println(pong.toString());
            }

            case NICK -> {
                NickMessage@Client nick = ch_AB.<NickMessage>tselect(
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
                JoinMessage@Client join = ch_AB.<JoinMessage>tselect(
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
                PartMessage@Client part = ch_AB.<PartMessage>tselect(
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
                PrivmsgMessage@Client privmsg = ch_AB.<PrivmsgMessage>tselect(
                    Util@Server.<PrivmsgMessage>as(msg));

                clientState.getOut().println(privmsg.toString());
            }

            case RPL_WELCOME -> {
                RplWelcomeMessage@Client welcome = ch_AB.<RplWelcomeMessage>tselect(
                    Util@Server.<RplWelcomeMessage>as(msg));

                clientState.getOut().println(welcome.toString());

                if (welcome.hasEnoughParams()) {
                    clientState.setNickname(welcome.getNickname());
                    clientState.setRegistered();
                }
            }

            case RPL_NAMREPLY -> {
                RplNamReplyMessage@Client namReply = ch_AB.<RplNamReplyMessage>tselect(
                    Util@Server.<RplNamReplyMessage>as(msg));

                clientState.getOut().println(namReply.toString());

                String@Client channel = namReply.getChannel();

                if (namReply.hasEnoughParams() && clientState.inChannel(channel)) {
                    clientState.addMembers(channel, namReply.getNicknames());
                }
            }

            case QUIT -> {
                QuitMessage@Client quit = ch_AB.<QuitMessage>tselect(
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
                ErrorMessage@Client error = ch_AB.<ErrorMessage>tselect(
                    Util@Server.<ErrorMessage>as(msg));

                String@Client info = "Disconnected"@Client;

                if (error.hasEnoughParams()) {
                    info = info + " ("@Client + error.getReason() + ")"@Client;
                }

                clientState.getOut().println(info);

                // Set the quitRequested flags to communicate a graceful close.
                serverState.setQuitRequested(clientId);
                clientState.setQuitRequested();
                ch_AB.close();
            }

            // NOTE: Any message that falls under ForwardMessage
            default -> {
                // We can use whichever ForwardMessage for the selection
                ForwardMessage@Client forward = ch_AB.<ForwardMessage>tselect(
                    Util@Server.<ForwardMessage>as(msg));

                Util@Server.check(ForwardMessage@Server.COMMANDS.contains(cmd),
                                  "Expected a ForwardMessage"@Server);

                clientState.getOut().println(forward.toString());
            }
        }
    }
}
