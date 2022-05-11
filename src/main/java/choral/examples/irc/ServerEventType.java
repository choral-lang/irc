package choral.examples.irc;

enum ServerEventType {
    NICK_ERROR,
    USER_ERROR,
    JOIN,
    MESSAGE,
    ERR_NONICKNAMEGIVEN,
    ERR_ERRONEUSNICKNAME,
    ERR_NICKNAMEINUSE,
    NICK_SUCCESS
}
