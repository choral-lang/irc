package choral.examples.irc;

enum ServerEventType {
    PING,
    PONG,
    NICK_ERROR,
    USER_ERROR,
    JOIN,
    MESSAGE,
    FORWARD_MESSAGE,
    UNKNOWN
}
