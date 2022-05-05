package choral.examples.irc;

import choral.lang.Unit;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    public static void main(String[] args) {
        ClientState state = new ClientState("choralbot");
        ExecutorService executor = Executors.newCachedThreadPool();

        System.out.println("Connecting to the server");

        SerializerChannel_A ch = new SerializerChannel_A(
            KryoSerializer.getInstance(),
            new WrapperByteChannel_A(
                SocketByteChannel.connect("localhost", 12345)));

        System.out.println("Connected");
        Irc.runClient(new Irc_Client(ch, state, Unit.id()), executor);
        System.out.println("Disconnected");

        executor.shutdown();
    }
}
