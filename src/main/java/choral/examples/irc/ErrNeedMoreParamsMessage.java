package choral.examples.irc;

public class ErrNeedMoreParamsMessage extends Message {
    public ErrNeedMoreParamsMessage() {
        super(Message.ERR_NEEDMOREPARAMS);
    }

    public ErrNeedMoreParamsMessage(String src) {
        super(src, Message.ERR_NEEDMOREPARAMS);
    }
}
