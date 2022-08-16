package choral.examples.irc;

public class UnrecognizedMessageException extends RuntimeException {
    private Message message;

    UnrecognizedMessageException(Message message) {
        super("Unrecognized message: '" + message.toString() + "'");
        this.message = message;
    }

    public Message getIrcMessage() {
        return message;
    }
}
