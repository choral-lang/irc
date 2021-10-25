package choral.examples.irc;

public class Source {
    private String nick, user, host;

    private Source() {
        this.nick = "";
        this.user = "";
        this.host = "";
    }

    public Source(String nick) {
        this();
        this.nick = nick;
    }

    public Source(String nick, String host) {
        this(nick);
        this.host = host;
    }

    public Source(String nick, String user, String host) {
        this(nick, host);
        this.user = user;
    }

    public static Source parse(String source) {
        String[] hostParts = source.split("@", 2);

        if (hostParts.length == 1)
            return new Source(source);

        String[] userParts = hostParts[0].split("!", 2);

        if (userParts.length == 1)
            return new Source(hostParts[0], hostParts[1]);

        return new Source(userParts[0], userParts[1], hostParts[1]);
    }
}
