package choral.examples.irc;

import java.util.List;

public class NickMessage extends Message {
    public NickMessage(Source src, String nickname) {
        super(src, Message.NICK, List.of(nickname));
    }

    public NickMessage(String nickname) {
        this(new Source(), nickname);
    }

    public String getNickname() {
        return getParam(0);
    }

    public static NickMessage construct(Message m) {
        List<String> params = m.getParams();

        if (params.size() < 1)
            throw new NoNicknameGivenException();

        return new NickMessage(m.getSrc(), params.get(0));
    }
}
