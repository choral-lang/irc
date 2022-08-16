package choral.examples.irc;

public class InvalidMessageException extends RuntimeException {
    private String message;

    InvalidMessageException(String message) {
        super("Invalid message: '" + message + "'");
        this.message = message;
    }

    public String getIrcMessage() {
        return message;
    }
}
