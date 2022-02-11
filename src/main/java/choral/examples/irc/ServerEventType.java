package choral.examples.irc;

enum ServerEventType {
    NICK,
    USER,
    JOIN,
    MESSAGE,
    ERR_NONICKNAMEGIVEN,
    ERR_ERRONEUSNICKNAME,
    ERR_NICKNAMEINUSE,
    NICK_SUCCESS
}
