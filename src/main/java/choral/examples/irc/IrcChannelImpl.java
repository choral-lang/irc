package choral.examples.irc;

import choral.channels.SymChannelImpl;
import choral.lang.Unit;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IrcChannelImpl implements SymChannelImpl<Message> {
    private static final int MAX_SIZE = 512;
    private static final byte[] MARKER = new byte[] {0x0D, 0x0A};
    private static final String SELECT = "SELECT";

    private ByteChannel channel;
    private ByteBuffer inBuffer;
    private ByteBuffer outBuffer;
    private int current;

    IrcChannelImpl(ByteChannel channel) {
        this.channel = channel;
        this.inBuffer = ByteBuffer.allocate(MAX_SIZE);
        this.outBuffer = ByteBuffer.allocate(MAX_SIZE);
        this.current = -1;
    }

    @Override
    public <M extends Message> Unit com(M m) {
        try {
            // Clear the buffer
            outBuffer.clear();

            // Write the message (will stop on overflow)
            CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
            encoder.encode(CharBuffer.wrap(m.toString()), outBuffer, true);

            if (outBuffer.remaining() < 2)
                throw new RuntimeException(String.format(
                    "Message exceeds the maximum length of %d bytes",
                    MAX_SIZE));

            // Write the marker
            outBuffer.put(MARKER);

            // Put the buffer into "read mode"
            outBuffer.flip();

            // Write it out
            channel.write(outBuffer);
        }
        catch (IOException e) {
            throw new ChannelException(e);
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
            if (inBuffer.remaining() == 0)
                inBuffer.clear();

            try {
                if (channel.read(inBuffer) == -1)
                    throw new ChannelException("Channel closed while reading");
            }
            catch (IOException e) {
                throw new ChannelException(e);
            }

            // Put the buffer into "read mode" to try to find the marker
            inBuffer.flip();
            current = findMarker(inBuffer, MARKER);

            // Put the buffer back into "write mode" if no marker was found
            if (current == -1) {
                inBuffer.position(inBuffer.limit());
                inBuffer.limit(inBuffer.capacity());
            }
        }

        // Extract the current message
        ByteBuffer b = inBuffer.duplicate().limit(current);
        String s = StandardCharsets.UTF_8.decode(b).toString();

        // Advance the position and attempt to find another complete message
        inBuffer.position(current + MARKER.length);
        current = findMarker(inBuffer, MARKER);

        // Rotate the buffer if this was the last complete message
        if (current == -1)
            inBuffer.compact();

        // Parse the message
        Message m = Message.parse(s);

        if (m == null)
            throw new InvalidMessageException(s);

        // Construct the appropriate subclass of Message. SELECT messages are
        // completely internal to the channel implementation and are therefore
        // not modeled as a separate subclass of Message.
        if (!m.getCommand().equals(SELECT)) {
            Message m2 = Message.construct(m);

            if (m2 == null)
                throw new UnrecognizedMessageException(m);

            m = m2;
        }

        @SuppressWarnings("unchecked")
        M res = (M) m;
        return res;
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
