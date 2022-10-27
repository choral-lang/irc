package choral.examples.irc;

import choral.lang.Unit;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        executor.submit(() -> {
            System.out.println("Listening on " + HOST + ":" + PORT);

            while (listener.isOpen()) {
                try {
                    SocketChannel client = listener.accept();
                    client.configureBlocking(true);

                    System.out.println("Client connected");

                    executor.submit(() -> {
                        try {
                            IrcChannel_B ch = new IrcChannel_B(client);
                            Irc_Server irc = new Irc_Server(ch, Unit.id, state);
                            irc.run(executor);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            executor.shutdown();
        });

        System.out.println("Commands: /state, /quit");

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
            else if (cmd.equalsIgnoreCase("/quit")) {
                break;
            }
            else {
                System.out.println("Unrecognized command");
            }
        }

        System.out.println("Quitting");

        s.close();
        listener.close();
    }
}
