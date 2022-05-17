package choral.examples.irc;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import choral.channels.SymChannelImpl;
import choral.lang.Unit;

public class IrcChannelImpl implements SymChannelImpl<Message> {
    private static final int MAX_SIZE = 512;
    private static final byte[] MARKER = new byte[] {0x0D, 0x0A};

    private ByteChannel channel;
    private Writer writer;
    private ByteBuffer buffer;
    private int current;

    IrcChannelImpl(ByteChannel channel) {
        this.channel = channel;
        this.writer = Channels.newWriter(channel, StandardCharsets.UTF_8);
        this.buffer = ByteBuffer.allocate(MAX_SIZE);
        this.current = -1;
    }

    @Override
    public <M extends Message> Unit com(M m) {
        try {
            writer.write(m.serialize());
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return Unit.id;
    }

    @Override
    public <M extends Message> M com(Unit u) {
        return com();
    }

    private static int findMarker(ByteBuffer buffer, byte[] marker) {
        int limit = buffer.limit();
        int i = buffer.position(), m = 0;
        assert marker.length > 0;

        while (i < limit && m < marker.length) {
            m = buffer.get(i) == marker[m] ? m + 1 : 0;
            ++i;
        }

        return i < limit ? i : -1;
    }

    @Override
    public <M extends Message> M com() {
        while (current == -1) {
            // If the buffer filled up and no marker was seen yet, throw away.
            if (buffer.remaining() == 0)
                buffer.clear();

            try {
                if (channel.read(buffer) == -1)
                    throw new RuntimeException("Channel closed while reading");
            }
            catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }

            // Put the buffer into "read mode".
            buffer.flip();

            if ((current = findMarker(buffer, MARKER)) == -1) {
                // Put the buffer back into "write mode".
                buffer.position(buffer.limit());
                buffer.limit(buffer.capacity());
            }
        }

        ByteBuffer b = buffer.slice().limit(current - MARKER.length);
        buffer.position(current);

        if ((current = findMarker(buffer, MARKER)) == -1)
            // Rotate the buffer for the upcoming write.
            buffer.compact();

        String s = StandardCharsets.UTF_8.decode(b).toString();
        Message m = Message.construct(Message.parse(s));

        if (m == null)
            throw new UnrecognizedMessageException(s);

        try {
            @SuppressWarnings("unchecked")
            M res = (M) m;
            return res;
        }
        catch (ClassCastException e) {
            throw new UnexpectedMessageException(m);
        }
    }

    @Override
    public <T extends Enum<T>> Unit select(T m) {
        // TODO: Ideally, selections should be fused with communications, but
        // until we have compiler support for that, we have to implement at
        // least a basic form of selection if we want working projected code. A
        // naive implementation could just make use of Enum.ordinal().
        return null;
    }

    @Override
    public <T extends Enum<T>> T select(Unit m) {
        return select();
    }

    @Override
    public <T extends Enum<T>> T select() {
        // TODO: See comment in select(T).
        return null;
    }
}
