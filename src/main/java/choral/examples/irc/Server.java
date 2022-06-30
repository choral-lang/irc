package choral.examples.irc;

import choral.lang.Unit;
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
                            Irc.runServer(irc, executor);
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

        System.out.println("Press C-d to stop the server");

        Scanner s = new Scanner(System.in);
        s.hasNextLine();
        s.close();

        System.out.println("Quitting");
        // TODO: Disconnect properly.

        listener.close();
    }
}
