package choral.examples.irc;

import java.util.List;

public class NickMessage extends Message {
    public NickMessage(Source src, String nickname) {
        super(src, Message.NICK, List.of(nickname));
    }

    public NickMessage(String nickname) {
        this(new Source(), nickname);
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
