package choral.examples.irc;

public class ChannelException extends RuntimeException {
    ChannelException(String message) {
        super(message);
    }

    ChannelException(Throwable cause) {
        super(cause);
    }
}
