package choral.examples.irc;

import java.util.ArrayList;
import java.util.List;
import java.lang.StringBuilder;

public class Message {
    public String tags, src, cmd;
    public List<String> params;

    public Message() {
        this.tags = "";
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
    }

    public Message(String src, String cmd, List<String> params) {
        this(cmd, params);
        this.src = src;
    }

    public Message(String tags, String src, String cmd,
                   List<String> params) {
        this(src, cmd, params);
        this.tags = tags;
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
       Parse an IRC message as specified by the IRC protocol. Assume there's no
       trailing CRLF sequence.

       Return either a new `Message` instance or null if the message couldn't be
       parsed (invalid format, characters, etc.).
     */
    public static Message parse(String str) {
        // TODO: Fail when the string contains NUL, CR or LF characters. Fail
        // when a non-trailing parameter contains a colon character. Handle
        // escaped values within tags.

        int len = str.length();

        int i = 0;
        String tags = null, src = null, cmd = null;
        List<String> params = new ArrayList<>();

        while (true) {
            i = skipWhitespace(str, i);
            int j = -1;

            if (i == len)
                break;

            // Tags.
            if (str.charAt(i) == '@') {
                j = untilWhitespace(str, i + 1);
                tags = str.substring(i + 1, j);
            }
            else if (str.charAt(i) == ':') {
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
                // a colon can only start a trailing parameter.
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

        if (tags == null)
            tags = "";

        return new Message(tags, src, cmd, params);
    }
}
