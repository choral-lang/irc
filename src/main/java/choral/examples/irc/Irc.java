package choral.examples.irc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class Irc {
    private static void clientSendLoop(Irc_Client client) {
        ClientState state = client.getClientState();
        LinkedBlockingQueue<Message> queue = state.getQueue();

        while (true) {
            try {
                Message msg = Util.take(queue);

                // NOTE: Process the "poison pill".
                if (msg instanceof PoisonPill) {
                    break;
                }

                client.clientProcessOne(msg);
            }
            catch (ChannelException e) {
                if (!state.getExit()) {
                    e.printStackTrace();
                }

                break;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void clientRecvLoop(Irc_Client client) {
        ClientState state = client.getClientState();
        LinkedBlockingQueue<Message> queue = state.getQueue();

        while (true) {
            try {
                client.serverProcessOne();
            }
            catch (ChannelException e) {
                // NOTE: Add the "poison pill".
                Util.put(queue, new PoisonPill());

                if (!state.getExit()) {
                    e.printStackTrace();
                }

                break;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void serverSendLoop(Irc_Server server) {
        long clientId = server.getClientId();
        ServerState state = server.getServerState();
        LinkedBlockingQueue<Message> queue = state.getQueue(clientId);

        while (true) {
            try {
                Message msg = Util.take(queue);

                // NOTE: Process the "poison pill".
                if (msg instanceof PoisonPill) {
                    break;
                }

                server.serverProcessOne(msg);
            }
            catch (ChannelException e) {
                if (!state.getExit(clientId)) {
                    e.printStackTrace();
                }

                break;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void serverRecvLoop(Irc_Server server) {
        long clientId = server.getClientId();
        ServerState state = server.getServerState();
        LinkedBlockingQueue<Message> queue = state.getQueue(clientId);

        while (true) {
            try {
                server.clientProcessOne();
            }
            catch (UnrecognizedMessageException e) {
                if (state.isRegistered(clientId)) {
                    server.addServerMessage(ServerUtil.forwardNumeric(
                        Command.ERR_UNKNOWNCOMMAND,
                        state.getNickname(clientId),
                        e.getIrcMessage().getCommand(),
                        "Unknown command"));
                }

                e.printStackTrace();
            }
            catch (ChannelException e) {
                // NOTE: Add the "poison pill".
                Util.put(queue, new PoisonPill());

                if (!state.getExit(clientId)) {
                    ServerUtil.sendQuits(state, clientId, ServerUtil.withSource(
                        new QuitMessage("Client disconnected"),
                        new Source(ServerUtil.HOSTNAME)));
                    e.printStackTrace();
                }

                break;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void runClient(Irc_Client client, ExecutorService executor) {
        executor.execute(() -> Irc.clientSendLoop(client));
        executor.execute(() -> Irc.clientRecvLoop(client));
    }

    public static void runServer(Irc_Server server, ExecutorService executor) {
        Future<?> f1 = executor.submit(() -> Irc.serverSendLoop(server));
        Future<?> f2 = executor.submit(() -> Irc.serverRecvLoop(server));

        // NOTE: Clear the client from the server state only once both loops are
        // done, to prevent race conditions.
        executor.execute(() -> {
            try {
                f1.get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            try {
                f2.get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            long clientId = server.getClientId();
            System.out.println("Client disconnected");
            server.getServerState().quit(clientId);
        });
    }
}
