package choral.examples.irc;

public class UnexpectedMessageException extends RuntimeException {
    private Message message;

    UnexpectedMessageException(Message message) {
        super("Unrecognized message: '" + message.serialize() + "'");
        this.message = message;
    }

    public Message getIrcMessage() {
        return message;
    }
}
