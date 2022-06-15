package choral.examples.irc;

import choral.channels.SymChannelImpl;
import choral.lang.Unit;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IrcChannelImpl implements SymChannelImpl<Message> {
    private static final int MAX_SIZE = 512;
    private static final byte[] MARKER = new byte[] {0x0D, 0x0A};
    private static final String SELECT = "SELECT";

    private ByteChannel channel;
    private ByteBuffer buffer;
    private int current;

    IrcChannelImpl(ByteChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(MAX_SIZE);
        this.current = -1;
    }

    @Override
    public <M extends Message> Unit com(M m) {
        try {
            String s = m.toString() + "\r\n";
            byte[] b = s.getBytes(StandardCharsets.UTF_8);

            if (b.length > 512)
                throw new RuntimeException(
                    "Message exceeds the maximum length of 512 bytes");

            channel.write(ByteBuffer.wrap(b));
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

        return m == marker.length ? (i - marker.length) : -1;
    }

    @Override
    public <M extends Message> M com() {
        // Read until we have at least one complete message
        while (current == -1) {
            // If the buffer filled up and no marker was seen yet, throw away
            if (buffer.remaining() == 0)
                buffer.clear();

            try {
                if (channel.read(buffer) == -1)
                    throw new RuntimeException("Channel closed while reading");
            }
            catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }

            // Put the buffer into "read mode" to try to find the marker
            buffer.flip();
            current = findMarker(buffer, MARKER);

            // Put the buffer back into "write mode" if no marker was found
            if (current == -1) {
                buffer.position(buffer.limit());
                buffer.limit(buffer.capacity());
            }
        }

        // Extract the current message
        ByteBuffer b = buffer.duplicate().limit(current);
        String s = StandardCharsets.UTF_8.decode(b).toString();

        // Advance the position and attempt to find another complete message
        buffer.position(current + MARKER.length);
        current = findMarker(buffer, MARKER);

        // Rotate the buffer if this was the last complete message
        if (current == -1)
            buffer.compact();

        // Parse the message
        Message m = Message.parse(s);

        // SELECT messages are handled internally as part of the channel
        // implementation
        if (!m.getCommand().equals(SELECT))
            m = Message.construct(m);

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
    public <T extends Enum<T>> Unit select(T l) {
        return com(new Message(null, SELECT,
                               List.of(l.getClass().getName(), l.name())));
    }

    @Override
    public <T extends Enum<T>> T select(Unit l) {
        return select();
    }

    @Override
    public <T extends Enum<T>> T select() {
        Message m = com();

        assert m.getCommand().equals(SELECT);
        assert m.getParams().size() == 2;

        String className = m.getParam(0);
        String enumName = m.getParam(1);

        try {
            @SuppressWarnings("unchecked")
            Class<T> enumClass = (Class<T>) Class.forName(className);

            return Enum.valueOf(enumClass, enumName);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Selection failed -- couldn't find class '" + className + "'");
        }
        catch (ClassCastException e) {
            throw new RuntimeException(
                "Selection failed -- loaded wrong class");
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(
                "Selection failed -- couldn't find enum '" + enumName + "'");
        }
    }
}
