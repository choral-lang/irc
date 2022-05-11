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

    public String getUsername() {
        return getParam(0);
    }

    public String getRealname() {
        return getParam(3);
    }

    public static UserMessage construct(Message m) {
        List<String> params = m.getParams();

        if (params.size() < 4)
            throw new NeedMoreParamsException(
                "At least 4 parameters are expected");

        return new UserMessage(m.getSrc(), params.get(0), params.get(3));
    }
}
