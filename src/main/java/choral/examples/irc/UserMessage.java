package choral.examples.irc;

import java.util.List;

public class UserMessage extends Message {
    public UserMessage(String username, String realname) {
        super(null, Command.USER.string(), List.of(username, "0", "*", realname));
    }

    public UserMessage(Message message) {
        super(message);
        assert command.equals(Command.USER.string());
    }

    public boolean hasEnoughParams() {
        return params.size() >= 4;
    }

    public String getUsername() {
        assert params.size() >= 1;
        return getParam(0);
    }

    public String getRealname() {
        assert params.size() >= 4;
        return getParam(3);
    }
}
