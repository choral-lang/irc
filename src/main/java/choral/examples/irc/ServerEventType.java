package choral.examples.irc;

enum ServerEventType {
    PING,
    PONG,
    NICK,
    JOIN,
    PART,
    PRIVMSG,
    RPL_WELCOME,
    RPL_NAMREPLY,
    FORWARD_MESSAGE,
    UNKNOWN
}
