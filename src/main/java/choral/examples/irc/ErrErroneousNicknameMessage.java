package choral.examples.irc;

public class ErrErroneousNicknameMessage extends Message {
    public ErrErroneousNicknameMessage() {
        super(Message.ERR_ERRONEOUSNICKNAME);
    }

    public ErrErroneousNicknameMessage(String src) {
        super(src, Message.ERR_ERRONEOUSNICKNAME);
    }
}
