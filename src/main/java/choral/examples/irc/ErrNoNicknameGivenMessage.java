package choral.examples.irc;

public class ErrNoNicknameGivenMessage extends Message {
    public ErrNoNicknameGivenMessage() {
        super(Message.ERR_NONICKNAMEGIVEN);
    }

    public ErrNoNicknameGivenMessage(String src) {
        super(src, Message.ERR_NONICKNAMEGIVEN);
    }
}
