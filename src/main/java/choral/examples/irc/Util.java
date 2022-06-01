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
}
