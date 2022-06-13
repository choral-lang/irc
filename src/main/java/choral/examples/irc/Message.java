package choral.examples.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.StringBuilder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Message {
    public static final Map<Command, Class<? extends Message>> MESSAGES =
        new HashMap<>() {{
            put(Command.PING, PingMessage.class);
            put(Command.PONG, PongMessage.class);
            put(Command.NICK, NickMessage.class);
            put(Command.USER, UserMessage.class);
        }};

    protected Source source;
    protected String command;
    protected List<String> params;

    public Message(Source source, String command, List<String> params) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        if (params == null) {
            this.params = new ArrayList<>();
        }
        else if (params.size() > 15) {
            throw new IllegalArgumentException(
                "There should be at most 15 parameters");
        }

        this.source = source;
        this.command = command;
        this.params = params;
    }

    public Message(Message m) {
        this.source = m.source;
        this.command = m.command;
        this.params = new ArrayList<>(m.params);
    }

    public Source getSource() {
        return source;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getParams() {
        return params;
    }

    public String getParam(int n) {
        return params.get(n);
    }

    private static int untilWhitespace(String str, int i) {
        for (; i < str.length(); ++i)
            if (str.charAt(i) == ' ')
                break;

        return i;
    }

    private static int skipWhitespace(String str, int i) {
        for (; i < str.length(); ++i)
            if (str.charAt(i) != ' ')
                break;

        return i;
    }

    /**
     * Construct an instance of the appropriate subclass of <code>Message</code>
     * by switching on the value of the command.
     *
     * Return null if the command of the message is not one of the known
     * standard commands (@see <code>Command</code>). Return the given message
     * if its command does not have a specific <code>Message</code> subclass.
     * Otherwise, return the newly constructed instance of the appropriate
     * <code>Message</code> subclass.
     */
    public static Message construct(Message m) {
        String code = m.getCommand();
        Command command = Command.fromCode(code);

        if (command == null)
            return null;

        Class<? extends Message> cls = MESSAGES.get(command);

        if (cls == null)
            return m;

        try {
            Constructor<? extends Message> ctor =
                cls.getConstructor(Message.class);
            return ctor.newInstance(m);
        }
        catch (NoSuchMethodException | InstantiationException |
               IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse an IRC message as specified by the IRC protocol. Assume there's no
     * trailing <code>CRLF</code> sequence.
     *
     * Return either a new <code>Message</code> instance or null if the message
     * couldn't be parsed (missing command, invalid characters, etc.).
     *
     * See RFC 1459, section 2.3.1.
     */
    public static Message parse(String str) {
        int len = str.length();

        int i = 0;
        boolean hasSource = false;
        Source source = null;
        String command = null;
        List<String> params = new ArrayList<>();

        while (true) {
            i = skipWhitespace(str, i);
            int j = -1;

            if (i == len)
                break;

            char c = str.charAt(i);

            if (c == '\0' || c == '\r' || c == '\n') {
                return null;
            }

            if (c == ':') {
                // Parse the source
                if (!hasSource) {
                    j = untilWhitespace(str, i + 1);
                    source = Source.parse(str.substring(i + 1, j));
                    hasSource = true;
                }
                // Parse the trailing parameter
                else {
                    params.add(str.substring(i + 1));
                    break;
                }
            }
            // Parse the command
            else if (command == null) {
                j = untilWhitespace(str, i);
                command = str.substring(i, j);

                if (!Util.validCommand(command)) {
                    return null;
                }

                if (source == null)
                    hasSource = true;
            }
            // Parse a non-trailing parameter
            else {
                j = untilWhitespace(str, i);
                params.add(str.substring(i, j));
            }

            i = j;
        }

        if (command == null)
            return null;

        return new Message(source, command, params);
    }

    /**
     * Serialize the <code>Message</code> instance to a string following the
     * format specified by the IRC protocol, but leaving out the trailing
     * <code>CRLF</code> sequence.
     *
     * The last parameter, even if the only one, is always serialized as a
     * trailing parameter.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (source != null) {
            sb.append(":");
            sb.append(source.toString());
            sb.append(" ");
        }

        sb.append(command);

        for (int i = 0; i < params.size(); ++i) {
            sb.append(" ");

            if (i == params.size() - 1)
                sb.append(":");

            sb.append(params.get(i));
        }

        return sb.toString();
    }
}
