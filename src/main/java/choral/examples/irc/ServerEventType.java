package choral.examples.irc;

enum ServerEventType {
    PING,
    PONG,
    NICK_ERROR,
    USER_ERROR,
    JOIN,
    PART,
    MESSAGE,
    RPL_NAMREPLY,
    FORWARD_MESSAGE,
    UNKNOWN
}
