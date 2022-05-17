package choral.examples.irc;

public class UnrecognizedMessageException extends RuntimeException {
    private String message;

    UnrecognizedMessageException(String message) {
        super("Unrecognized message: '" + message + "'");
        this.message = message;
    }

    public String getIrcMessage() {
        return message;
    }
}
