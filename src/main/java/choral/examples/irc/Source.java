package choral.examples.irc;

public class Source {
    private String nickname, username, hostname;

    public Source(String nickname, String username, String hostname) {
        this.nickname = nickname;
        this.username = username;
        this.hostname = hostname;
    }

    public String getNickname() {
        return nickname;
    }

    public String getUsername() {
        return username;
    }

    public String getHostname() {
        return hostname;
    }

    public static Source fromNickname(String nickname) {
        return new Source(nickname, null, null);
    }

    public static Source fromHostname(String hostname) {
        return new Source(null, null, hostname);
    }

    public static Source parse(String str) {
        String host = null;
        String[] hostParts = str.split("@", 2);

        if (hostParts.length == 2) {
            host = hostParts[1];
        }
        else if (hostParts.length > 2) {
            return null;
        }

        String user = null;
        String[] userParts = hostParts[0].split("!", 2);

        if (userParts.length == 2) {
            user = userParts[1];
        }
        else if (userParts.length > 2) {
            return null;
        }

        return new Source(userParts[0], user, host);
    }

    public String toString() {
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
