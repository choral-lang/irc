package choral.examples.irc;

import choral.lang.Unit;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        ServerState state = new ServerState();
        ExecutorService executor = Executors.newCachedThreadPool();
        ServerSocketByteChannel listener = ServerSocketByteChannel
            .at("localhost", 12345);

        executor.submit(() -> {
            while (listener.isOpen()) {
                try {
                    System.out.println("Waiting for connections");

                    SerializerChannel_B ch = new SerializerChannel_B(
                        KryoSerializer.getInstance(),
                        new WrapperByteChannel_B(listener.getNext()));

                    executor.submit(() -> {
                        try {
                            System.out.println("Client connected");
                            Irc.runServer(
                                new Irc_Server(ch, Unit.id(), state), executor);
                            System.out.println("Client disconnected");
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

        listener.close();
    }
}
