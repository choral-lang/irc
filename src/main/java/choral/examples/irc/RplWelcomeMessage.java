package choral.examples.irc;

import java.util.List;

public class RplWelcomeMessage extends Message {
    public RplWelcomeMessage(String nickname, String message) {
        super(null, Command.RPL_WELCOME.string(), List.of(nickname, message));
    }

    public RplWelcomeMessage(Message message) {
        super(message);
        assert command.equals(Command.RPL_WELCOME.string());
    }

    public boolean hasEnoughParams() {
        return params.size() >= 2;
    }

    public String getNickname() {
        assert params.size() >= 1;
        return params.get(0);
    }

    public String getMessage() {
        assert params.size() >= 2;
        return params.get(1);
    }
}
