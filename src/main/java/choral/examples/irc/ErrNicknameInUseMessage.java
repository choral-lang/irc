package choral.examples.irc;

public class ErrNicknameInUseMessage extends Message {
    public ErrNicknameInUseMessage() {
        super(Message.ERR_NICKNAMEINUSE);
    }

    public ErrNicknameInUseMessage(String src) {
        super(src, Message.ERR_NICKNAMEINUSE);
    }
}
