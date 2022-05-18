package choral.examples.irc;

public class Util {
    public static boolean validNickname(String nickname) {
        return nickname.matches("[a-zA-Z0-9\\-\\\\`^\\[\\]{}]+");
    }
}
