package choral.examples.irc;

public class Source {
    private String nickname, username, hostname;

    public Source() {
        this.nickname = null;
        this.username = null;
        this.hostname = null;
    }

    public Source(String nickname) {
        this();
        this.nickname = nickname;
    }

    public Source(String nickname, String hostname) {
        this(nickname);
        this.hostname = hostname;
    }

    public Source(String nickname, String username, String hostname) {
        this(nickname, hostname);
        this.username = username;
    }

    public static Source parse(String str) {
        String[] hostParts = str.split("@", 2);

        if (hostParts.length == 1)
            return new Source(str);

        String[] userParts = hostParts[0].split("!", 2);

        if (userParts.length == 1)
            return new Source(hostParts[0], hostParts[1]);

        return new Source(userParts[0], userParts[1], hostParts[1]);
    }

    public String serialize() {
        return nickname + "@" + username + "!" + hostname;
    }
}
