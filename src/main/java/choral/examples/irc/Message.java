package choral.examples.irc;

import java.util.ArrayList;
import java.util.List;
import java.lang.StringBuilder;

public class Message {
    public static final String NICK = "NICK";
    public static final String USER = "USER";

    public static final String ERR_NONICKNAMEGIVEN = "431";
    public static final String ERR_ERRONEUSNICKNAME = "432";
    public static final String ERR_NICKNAMEINUSE = "433";

    private String src, cmd;
    private List<String> params;

    public Message() {
        this.src = "";
        this.cmd = "";
        this.params = new ArrayList<>();
    }

    public Message(String cmd) {
        this();
        this.cmd = cmd;
    }

    public Message(String cmd, List<String> params) {
        this(cmd);
        this.params = params;

        if (params.size() > 15)
            throw new IllegalArgumentException("There should be at most 15 parameters");
    }

    public Message(String src, String cmd, List<String> params) {
        this(cmd, params);
        this.src = src;
    }

    public String getSrc() {
        return src;
    }

    public String getCommand() {
        return cmd;
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
        String cmd = m.getCommand();

        if (cmd == NICK) {
            return NickMessage.construct(m);
        }
        else if (cmd == USER) {
            return UserMessage.construct(m);
        }

        return null;
    }

    /**
       Parse an IRC message as specified by the IRC protocol. Assume there's no
       trailing CRLF sequence.

       Return either a new `Message` instance or null if the message couldn't be
       parsed (invalid format, characters, etc.).
     */
    public static Message parse(String str) {
        // TODO: Fail when the string contains NUL, CR or LF characters. Fail
        // when a non-trailing parameter contains a colon character.

        int len = str.length();

        int i = 0;
        String src = null, cmd = null;
        List<String> params = new ArrayList<>();

        while (true) {
            i = skipWhitespace(str, i);
            int j = -1;

            if (i == len)
                break;

            if (str.charAt(i) == ':') {
                // Source.
                if (src == null) {
                    j = untilWhitespace(str, i + 1);
                    src = str.substring(i + 1, j);
                }
                // Trailing parameter.
                else {
                    params.add(str.substring(i + 1));
                    break;
                }
            }
            // Command.
            else if (cmd == null) {
                j = untilWhitespace(str, i);
                cmd = str.substring(i, j);

                // Treat an unspecified source as empty. The next occurrence of
                // a colon can then only start a trailing parameter.
                if (src == null)
                    src = "";
            }
            // Parameter.
            else {
                j = untilWhitespace(str, i);
                params.add(str.substring(i, j));
            }

            i = j;
        }

        if (cmd == null)
            return null;

        return new Message(src, cmd, params);
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

        if (!src.isEmpty()) {
            sb.append(":");
            sb.append(src);
            sb.append(" ");
        }

        sb.append(cmd);

        for (int i = 0; i < params.size(); ++i) {
            sb.append(" ");

            if (i == params.size() - 1)
                sb.append(":");

            sb.append(params.get(i));
        }

        sb.append("\r\n");

        return sb.toString();
    }

    public static Message prepareNick(String nickname) {
        return new Message("NICK", List.of(nickname));
    }

    public static Message prepareUser(String username, String realname) {
        return new Message("USER", List.of(username, "0", "*", realname));
    }

    public static Message prepareJoin(List<String> channels, List<String> keys) {
        return new Message("JOIN", List.of(String.join(",", channels),
                                           String.join(",", keys)));
    }
}
