package choral.examples.irc;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final String HOST = "localhost";
    private static final int PORT = 8667;

    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);
        Gson gson = new Gson();
        ServerState state = new ServerState();
        ExecutorService executor = Executors.newCachedThreadPool();

        ServerSocketChannel listener = ServerSocketChannel.open();
        listener.bind(new InetSocketAddress(HOST, PORT));

        executor.execute(() -> {
            System.out.println("Listening on " + HOST + ":" + PORT);

            while (listener.isOpen()) {
                try {
                    SocketChannel client = listener.accept();
                    client.configureBlocking(true);

                    executor.execute(() -> {
                        IrcChannel_B ch = new IrcChannel_B(client);
                        Irc_Server irc = new Irc_Server(ch);
                        long clientId = state.newClient(ch, irc.serverQueue());

                        System.out.println("Client connected: " + clientId);
                        irc.run(state, clientId, executor);
                    });
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            executor.shutdown();
        });

        System.out.println("Commands: /state, /exit");

        while (true) {
            System.out.print("> ");

            if (!s.hasNextLine())
                break;

            String line = s.nextLine();
            String[] parts = line.split(" +");

            if (parts.length == 0) {
                System.out.println("Invalid command");
                continue;
            }

            String cmd = parts[0];

            if (cmd.equalsIgnoreCase("/state")) {
                System.out.println(gson.toJson(state));
            }
            else if (cmd.equalsIgnoreCase("/exit")) {
                break;
            }
            else {
                System.out.println("Unrecognized command");
            }
        }

        System.out.println("Exiting");

        s.close();
        listener.close();

        for (long clientId : state.clients()) {
            state.addMessage(clientId, ServerUtil.withSource(
                new ErrorMessage("Server closing"),
                new Source(ServerUtil.HOSTNAME)));
            state.setQuitRequested(clientId);
            state.stop(clientId);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e2) {
            // Ignore
        }
    }
}
