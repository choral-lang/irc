package choral.examples.irc;

import java.util.List;

// TODO: Validate username and realname.

public class RegisterMessage extends Message {
    public static final String CMD = "USER";

    public RegisterMessage(String src, String username, String realname) {
        super(src, "USER", List.of(username, "0", "*", realname));
    }

    public RegisterMessage(String username, String realname) {
        this("", username, realname);
    }

    public String getUsername() {
        return getParam(0);
    }

    public String getRealname() {
        return getParam(3);
    }

    public static RegisterMessage construct(Message m) {
        List<String> params = m.getParams();

        if (params.size() < 4)
            throw new IllegalArgumentException(
                "At least 4 parameters are expected");

        return new RegisterMessage(m.getSrc(), params.get(0), params.get(3));
    }
}
