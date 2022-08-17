package choral.examples.irc;

public class InvalidMessageException extends RuntimeException {
    private String string;

    InvalidMessageException(String string) {
        super("Invalid message: '" + string + "'");
        this.string = string;
    }

    public String getString() {
        return string;
    }
}
