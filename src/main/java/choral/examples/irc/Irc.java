package choral.examples.irc;

import java.util.concurrent.ExecutorService;

public class Irc {
    public static void runClient(Irc_Client client, ExecutorService executor) {
        executor.execute(client::clientDrivenLoop);
        executor.execute(client::serverDrivenLoop);
    }

    public static void runServer(Irc_Server server, ExecutorService executor) {
        executor.execute(server::clientDrivenLoop);
        executor.execute(server::serverDrivenLoop);
    }
}
