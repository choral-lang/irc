package choral.examples.irc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.StringBuilder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Message {
    public static final String NICK = "NICK";
    public static final String USER = "USER";

    public static final String RPL_WELCOME = "001";
    public static final String RPL_YOURHOST = "002";
    public static final String RPL_CREATED = "003";
    public static final String RPL_MYINFO = "004";
    public static final String RPL_ISUPPORT = "005";
    public static final String RPL_UMODEIS = "221";

    public static final String RPL_LUSERCLIENT = "251";
    public static final String RPL_LUSEROP = "252";
    public static final String RPL_LUSERUNKNOWN = "253";
    public static final String RPL_LUSERCHANNELS = "254";
    public static final String RPL_LUSERME = "255";
    public static final String RPL_LOCALUSERS = "265";
    public static final String RPL_GLOBALUSERS = "266";

    public static final String RPL_MOTD = "372";
    public static final String RPL_MOTDSTART = "375";
    public static final String RPL_ENDOFMOTD = "376";
    public static final String ERR_NOMOTD = "422";

    public static final String ERR_NONICKNAMEGIVEN = "431";
    public static final String ERR_ERRONEOUSNICKNAME = "432";
    public static final String ERR_NICKNAMEINUSE = "433";
    public static final String ERR_NEEDMOREPARAMS = "461";
    public static final String ERR_ALREADYREGISTERED = "462";

    public static final Map<String, Class<? extends Message>> MESSAGES = new HashMap<>() {{
        put(NICK, NickMessage.class);
        put(USER, UserMessage.class);
        put(RPL_WELCOME, Message.class);
        put(RPL_YOURHOST, Message.class);
        put(RPL_CREATED, Message.class);
        put(RPL_MYINFO, Message.class);
        put(RPL_ISUPPORT, Message.class);
        put(RPL_UMODEIS, Message.class);

        put(RPL_LUSERCLIENT, Message.class);
        put(RPL_LUSEROP, Message.class);
        put(RPL_LUSERUNKNOWN, Message.class);
        put(RPL_LUSERCHANNELS, Message.class);
        put(RPL_LUSERME, Message.class);
        put(RPL_LOCALUSERS, Message.class);
        put(RPL_GLOBALUSERS, Message.class);

        put(RPL_MOTD, Message.class);
        put(RPL_MOTDSTART, Message.class);
        put(RPL_ENDOFMOTD, Message.class);
        put(ERR_NOMOTD, Message.class);

        put(ERR_NONICKNAMEGIVEN, Message.class);
        put(ERR_ERRONEOUSNICKNAME, Message.class);
        put(ERR_NICKNAMEINUSE, Message.class);
        put(ERR_NEEDMOREPARAMS, Message.class);
        put(ERR_ALREADYREGISTERED, Message.class);
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
       Construct an instance of the appropriate subclass of `Message` by
       switching on the value of the command.

       Return the newly constructed instance or null if the command doesn't
       match any of the known subclasses.
     */
    public static Message construct(Message m) {
        Class<? extends Message> cls = MESSAGES.get(m.getCommand());

        if (cls == null)
            return null;

        try {
            Constructor<? extends Message> ctor = cls.getConstructor(Message.class);
            return ctor.newInstance(m);
        }
        catch (NoSuchMethodException | InstantiationException |
               IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
       Parse an IRC message as specified by the IRC protocol. Assume there's no
       trailing CRLF sequence.

       Return either a new `Message` instance or null if the message couldn't be
       parsed (missing command, invalid characters, etc.).

       See RFC 1459, section 2.3.1.
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
       Serialize the `Message` instance to a string following the format
       specified by the IRC protocol.

       The last parameter, even if the only one, is always serialized as a
       trailing parameter.
     */
    public String serialize() {
        // TODO: Make sure there are no NUL, CR or LF characters in the message
        // parts.
        StringBuilder sb = new StringBuilder();

        if (source != null) {
            sb.append(":");
            sb.append(source.serialize());
            sb.append(" ");
        }

        sb.append(command);

        for (int i = 0; i < params.size(); ++i) {
            sb.append(" ");

            if (i == params.size() - 1)
                sb.append(":");

            sb.append(params.get(i));
        }

        sb.append("\r\n");

        return sb.toString();
    }
}
