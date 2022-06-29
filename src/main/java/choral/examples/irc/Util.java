package choral.examples.irc;

import java.util.concurrent.LinkedBlockingQueue;

public class Util {
    public static <T> T take(LinkedBlockingQueue<T> queue) {
        while (true) {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                // Ignore and keep trying
            }
        }
    }

    public static <T> void put(LinkedBlockingQueue<T> queue, T item) {
        while (true) {
            try {
                queue.put(item);
                break;
            } catch (InterruptedException e) {
                // Ignore and keep trying
            }
        }
    }

    /**
     * Check whether a command is valid.
     *
     * A valid command is a sequence of one or more ASCII letters, or a sequence
     * of exactly 3 ASCII digits.
     *
     * See RFC 1459, section 2.3.1.
     */
    public static boolean validCommand(String command) {
        return command.matches("[a-zA-Z]+|\\d{3}");
    }

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

    /**
     * Check whether a channel name is valid.
     *
     * A valid channel name starts with either '&' or '#' and is followed by one
     * or more non-control non-whitespace ASCII characters (characters between
     * SPC (0x20) and DEL (0x7F), exclusive), and also cannot include commas.
     *
     * This differs from RFC 1459 where channel names can contain ASCII control
     * characters.
     *
     * See RFC 1459, section 2.3.1.
     */
    public static boolean validChannelname(String channel) {
        // SPC (\x20), '!' (\x21), ..., '+' (\x2B), ',' (\x2C), '-' (\x2D), ...,
        // DEL (\x7E)
        return channel.matches("(?:&|#)[\\x21-\\x2B\\x2D-\\x7E]+");
    }
}
