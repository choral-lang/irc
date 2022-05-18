package choral.examples.irc;

import java.util.ArrayList;
import java.util.List;
import java.lang.StringBuilder;

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

    protected Source source;
    protected String command;
    protected List<String> params;

    public Message(String command) {
        this.source = null;
        this.command = command;
        this.params = new ArrayList<>();
    }

    public Message(String command, List<String> params) {
        this(command);
        this.params = params;

        if (params.size() > 15)
            throw new IllegalArgumentException("There should be at most 15 parameters");
    }

    public Message(String command, String param) {
        this(command, List.of(param));
    }

    public Message(String command, String param1, String param2) {
        this(command, List.of(param1, param2));
    }

    public Message(Source source, String command) {
        this(source, command, List.of());
    }

    public Message(Source source, String command, List<String> params) {
        this(command, params);
        this.source = source;
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
        String command = m.getCommand();

        if (command == NICK) {
            return new NickMessage(m);
        }
        else if (command == USER) {
            return new UserMessage(m);
        }

        return null;
    }

    /**
       Parse an IRC message as specified by the IRC protocol. Assume there's no
       trailing CRLF sequence.

       Return either a new `Message` instance or null if the message couldn't be
       parsed (missing command, invalid characters, etc.).

       See RFC 1459, section 2.3.1.
     */
    public static Message parse(String str) {
        // TODO: Fail when the string contains NUL, CR or LF characters. Fail
        // when a non-trailing parameter contains a colon character.

        int len = str.length();

        int i = 0;
        String source = null, command = null;
        List<String> params = new ArrayList<>();

        while (true) {
            i = skipWhitespace(str, i);
            int j = -1;

            if (i == len)
                break;

            if (str.charAt(i) == ':') {
                // Source.
                if (source == null) {
                    j = untilWhitespace(str, i + 1);
                    source = str.substring(i + 1, j);
                }
                // Trailing parameter.
                else {
                    params.add(str.substring(i + 1));
                    break;
                }
            }
            // Command.
            else if (command == null) {
                j = untilWhitespace(str, i);
                command = str.substring(i, j);

                // Treat an unspecified source as empty. The next occurrence of
                // a colon can then only start a trailing parameter.
                if (source == null)
                    source = "";
            }
            // Parameter.
            else {
                j = untilWhitespace(str, i);
                params.add(str.substring(i, j));
            }

            i = j;
        }

        if (command == null)
            return null;

        return new Message(Source.parse(source), command, params);
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
