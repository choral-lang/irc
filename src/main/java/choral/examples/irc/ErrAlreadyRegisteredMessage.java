package choral.examples.irc;

public class ErrAlreadyRegisteredMessage extends Message {
    public ErrAlreadyRegisteredMessage() {
        super(Message.ERR_ALREADYREGISTERED);
    }
}
