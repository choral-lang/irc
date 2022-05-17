package choral.examples.irc;

import java.util.List;

public class NickMessage extends Message {
    public NickMessage(String nickname) {
        super(Message.NICK, List.of(nickname));
    }

    public NickMessage(Message message) {
        super(message);
    }

    public boolean hasEnoughParams() {
        return params.size() >= 1;
    }

    public String getNickname() {
        return getParam(0);
    }
}
