package choral.examples.irc;

// NOTE: See RFC 1459, section 2.3.1.

public class Util {
    public static boolean validNickname(String nickname) {
        return nickname.matches("[a-zA-Z0-9\\-\\\\`^\\[\\]{}]+");
    }
}
