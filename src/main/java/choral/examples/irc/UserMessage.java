package choral.examples.irc;

import java.util.List;

// TODO: Validate username and realname.

public class UserMessage extends Message {
    public UserMessage(Source src, String username, String realname) {
        super(src, Message.USER, List.of(username, "0", "*", realname));
    }

    public UserMessage(String username, String realname) {
        this(new Source(), username, realname);
    }

    public UserMessage(Message message) {
        super(message);
    }

    public boolean hasEnoughParams() {
        return params.size() >= 4;
    }

    public String getUsername() {
        return getParam(0);
    }

    public String getRealname() {
        return getParam(3);
    }
}
