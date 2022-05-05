package choral.examples.irc;

import java.util.concurrent.ExecutorService;

public class Irc {
    public static void runClient(Irc_Client client, ExecutorService executor) {
        executor.submit(() -> {
            client.clientDrivenLoop();
            client.serverDrivenLoop();
        });
    }

    public static void runServer(Irc_Server server, ExecutorService executor) {
        executor.submit(() -> {
            server.clientDrivenLoop();
            server.serverDrivenLoop();
            server.serverLocalLoop();
        });
    }
}
