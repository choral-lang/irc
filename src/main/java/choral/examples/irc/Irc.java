package choral.examples.irc;

import java.util.concurrent.ExecutorService;

public class Irc {
    private static void defaultLoop(Runnable body) {
        while (true) {
            try {
                body.run();
            }
            catch (InvalidMessageException | UnexpectedMessageException |
                   UnrecognizedMessageException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private static void clientSendLoop(Irc_Client client) {
        Irc.defaultLoop(() -> client.clientProcessOne());
    }

    private static void clientRecvLoop(Irc_Client client) {
        Irc.defaultLoop(() -> client.serverProcessOne());
    }

    private static void serverSendLoop(Irc_Server server) {
        Irc.defaultLoop(() -> server.serverProcessOne());
    }

    private static void serverRecvLoop(Irc_Server server) {
        while (true) {
            try {
                server.clientProcessOne();
            }
            catch (InvalidMessageException | UnexpectedMessageException e) {
                e.printStackTrace();
            }
            catch (UnrecognizedMessageException e) {
                long clientId = server.getClientId();
                ServerState state = server.getServerState();

                if (state.isRegistered(clientId)) {
                    server.addServerMessage(ServerUtil.forwardNumeric(
                        Command.ERR_UNKNOWNCOMMAND,
                        state.getNickname(clientId),
                        e.getIrcMessage().getCommand(),
                        "Unknown command"));
                }

                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void runClient(Irc_Client client, ExecutorService executor) {
        executor.execute(() -> Irc.clientSendLoop(client));
        executor.execute(() -> Irc.clientRecvLoop(client));
    }

    public static void runServer(Irc_Server server, ExecutorService executor) {
        executor.execute(() -> Irc.serverSendLoop(server));
        executor.execute(() -> Irc.serverRecvLoop(server));
    }
}
