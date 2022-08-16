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

    private static void clientSendLoop(Irc_Client client, IrcChannel_A ch) {
        Irc.defaultLoop(() -> client.clientProcessOne(ch));
    }

    private static void clientRecvLoop(Irc_Client client, IrcChannel_A ch) {
        Irc.defaultLoop(() -> client.serverProcessOne(ch));
    }

    private static void serverSendLoop(Irc_Server server, IrcChannel_B ch) {
        Irc.defaultLoop(() -> server.serverProcessOne(ch));
    }

    private static void serverRecvLoop(Irc_Server server, IrcChannel_B ch) {
        Irc.defaultLoop(() -> server.clientProcessOne(ch));
    }

    public static void runClient(Irc_Client client, IrcChannel_A ch,
                                 ExecutorService executor) {
        executor.execute(() -> Irc.clientSendLoop(client, ch));
        executor.execute(() -> Irc.clientRecvLoop(client, ch));
    }

    public static void runServer(Irc_Server server, IrcChannel_B ch,
                                 ExecutorService executor) {
        executor.execute(() -> Irc.serverSendLoop(server, ch));
        executor.execute(() -> Irc.serverRecvLoop(server, ch));
    }
}
