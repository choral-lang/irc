package choral.examples.irc;

import java.util.List;

public class NickMessage extends Message {
    public NickMessage(String nickname) {
        super(null, Command.NICK.code(), List.of(nickname));
    }

    public NickMessage(Message message) {
        super(message);
        assert command == Command.NICK.code();
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public String getNickname() {
        assert params.size() >= 1;
        return getParam(0);
    }
}
