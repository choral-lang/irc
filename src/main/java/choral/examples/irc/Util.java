package choral.examples.irc;

public class Util {
    /**
     * Check whether a nickname is valid.
     *
     * A valid nickname starts with an ASCII letter and is followed by one or
     * more of the following characters: 'a'-'z', 'A'-'Z', '0'-'9', '-', '_',
     * '\', '`', '^', '[', ']', '{' or '}'.
     *
     * See RFC 1459, section 2.3.1.
     */
    public static boolean validNickname(String nickname) {
        return nickname.matches("[a-zA-z][a-zA-Z0-9\\-_\\\\`^\\[\\]{}]*");
    }

    /**
     * Check whether a username is valid.
     *
     * A valid username is a sequence of one or more non-control non-whitespace
     * ASCII characters (characters between SPC (0x20) and DEL (0x7F),
     * exclusive), also excluding '@'.
     *
     * This differs from RFC 1459 where channel names can contain ASCII control
     * characters or '@'.
     *
     * See RFC 1459, section 2.3.1.
     */
    public static boolean validUsername(String username) {
        // SPC (\x20), '!' (\x21), ..., '?' (\x3F), '@' (\x40), 'A' (\x41), ...,
        // DEL (\x7E)
        return username.matches("[\\x21-\\x3F\\x41-\\x7E]+");

    }
}
