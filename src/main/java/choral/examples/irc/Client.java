package choral.examples.irc;

import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;

public class Client {
    public static void main(String[] args) {
        System.out.println("Connecting to the server");

        SerializerChannel_A ch = new SerializerChannel_A(
            KryoSerializer.getInstance(),
            new WrapperByteChannel_A(
                SocketByteChannel.connect("localhost", 12345)));

        System.out.println("Client connected");
        new Irc_Client(ch).run();
        System.out.println("Client disconnected");
    }
}
