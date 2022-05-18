package choral.examples.irc;

public class Source {
    private String nickname, username, hostname;

    public Source(String nickname) {
        this.nickname = nickname;
        this.username = null;
        this.hostname = null;
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
        StringBuilder sb = new StringBuilder();

        sb.append(nickname);

        if (username != null) {
            sb.append("!");
            sb.append(username);
        }

        if (hostname != null) {
            sb.append("@");
            sb.append(hostname);
        }

        return sb.toString();
    }
}
