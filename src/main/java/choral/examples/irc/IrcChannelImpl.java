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
    private static final byte[] CRLF = new byte[] {0x0D, 0x0A};
    private static final byte[] LF = new byte[] {0x0A};
    private static final String SELECT = "SELECT";

    private ByteChannel channel;
    private ByteBuffer inBuffer;
    private ByteBuffer outBuffer;
    private byte[] marker;
    private int current;

    public IrcChannelImpl(ByteChannel channel) {
        this.channel = channel;
        this.inBuffer = ByteBuffer.allocate(MAX_SIZE);
        this.outBuffer = ByteBuffer.allocate(MAX_SIZE);
        this.marker = null;
        this.current = -1;
    }

    @Override
    public <M extends Message> Unit com(M message) {
        try {
            // Clear the buffer
            outBuffer.clear();

            // We truncate large messages
            outBuffer.limit(MAX_SIZE - CRLF.length);

            // Write the message (the encoder stops when it reaches the limit)
            CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
            encoder.encode(CharBuffer.wrap(message.toString()),
                           outBuffer, true);

            // Write the marker
            outBuffer.limit(MAX_SIZE);
            outBuffer.put(CRLF);

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

    private void findNext() {
        marker = CRLF;
        current = findMarker(inBuffer, marker);

        if (current == -1) {
            marker = LF;
            current = findMarker(inBuffer, marker = LF);
        }
    }

    @Override
    public <M extends Message> M com() {
        // Read until we have at least one complete message
        while (current == -1) {
            // If the buffer filled up and no marker was seen yet, throw away
            if (inBuffer.remaining() == 0) {
                inBuffer.clear();
            }

            try {
                if (channel.read(inBuffer) == -1) {
                    throw new ChannelException("Channel closed while reading");
                }
            }
            catch (IOException e) {
                throw new ChannelException(e);
            }

            // Put the buffer into "read mode" to try to find the marker
            inBuffer.flip();
            findNext();

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
        inBuffer.position(current + marker.length);
        findNext();

        // Rotate the buffer if this was the last complete message
        if (current == -1) {
            inBuffer.compact();
        }

        // Parse the message
        Message m = Message.parse(s);

        if (m == null) {
            throw new InvalidMessageException(s);
        }

        // Construct the appropriate subclass of Message. SELECT messages are
        // completely internal to the channel implementation and are therefore
        // not modeled as a separate subclass of Message.
        if (!m.getCommand().equals(SELECT)) {
            Message m2 = Message.construct(m);

            if (m2 == null) {
                throw new UnrecognizedMessageException(m);
            }

            m = m2;
        }

        @SuppressWarnings("unchecked")
        M res = (M) m;
        return res;
    }

    @Override
    public <T extends Enum<T>> Unit select(T label) {
        return com(new Message(
            null, SELECT, List.of(label.getClass().getName(), label.name())));
    }

    @Override
    public <T extends Enum<T>> T select(Unit u) {
        return select();
    }

    @Override
    public <T extends Enum<T>> T select() {
        Message m = com();

        if (!m.getCommand().equals(SELECT)) {
            throw new RuntimeException(
                "Selection failed -- expected a SELECT message");
        }

        if (m.getParams().size() != 2) {
            throw new RuntimeException(
                "Selection failed -- not enough parameters");
        }

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

    public <M extends Message> Unit tselect(M message) {
        return com(message);
    }

    public <M extends Message> M tselect(Unit u) {
        return tselect();
    }

    public <M extends Message> M tselect() {
        return com();
    }

    public boolean isOpen() {
        return channel.isOpen();
    }

    public void close() {
        try {
            channel.close();
        }
        catch (IOException e) {
            throw new ChannelException(e);
        }
    }
}
