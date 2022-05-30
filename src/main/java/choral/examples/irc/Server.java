package choral.examples.irc;

import choral.lang.Unit;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String HOST = "localhost";
    private static final int PORT = 8667;

    public static void main(String[] args) {
        ServerState state = new ServerState();
        ExecutorService executor = Executors.newCachedThreadPool();
        ServerSocketByteChannel listener = ServerSocketByteChannel.at(HOST, PORT);

        executor.submit(() -> {
            System.out.println("Listening on " + HOST + ":" + PORT);

            while (listener.isOpen()) {
                try {
                    IrcChannel_B ch = new IrcChannel_B(listener.getNext());

                    System.out.println("Client connected");

                    executor.submit(() -> {
                        try {
                            Irc.runServer(new Irc_Server(ch, Unit.id, state),
                                          executor);
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
