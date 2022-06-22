package choral.examples.irc;

import java.util.HashMap;
import java.util.Map;

public enum Command {
    PING("PING"),
    PONG("PONG"),
    NICK("NICK"),
    USER("USER"),
    JOIN("JOIN"),
    PART("PART"),
    PRIVMSG("PRIVMSG"),

    RPL_WELCOME("001"),
    RPL_YOURHOST("002"),
    RPL_CREATED("003"),
    RPL_MYINFO("004"),
    RPL_ISUPPORT("005"),
    RPL_UMODEIS("221"),

    RPL_LUSERCLIENT("251"),
    RPL_LUSEROP("252"),
    RPL_LUSERUNKNOWN("253"),
    RPL_LUSERCHANNELS("254"),
    RPL_LUSERME("255"),
    RPL_LOCALUSERS("265"),
    RPL_GLOBALUSERS("266"),

    RPL_NAMREPLY("353"),
    RPL_ENDOFNAMES("366"),
    RPL_MOTD("372"),
    RPL_MOTDSTART("375"),
    RPL_ENDOFMOTD("376"),

    ERR_NOSUCHNICK("401"),
    ERR_NOSUCHCHANNEL("403"),
    ERR_CANNOTSENDTOCHAN("404"),
    ERR_NORECIPIENT("411"),
    ERR_NOTEXTTOSEND("412"),
    ERR_NOMOTD("422"),
    ERR_NONICKNAMEGIVEN("431"),
    ERR_ERRONEOUSNICKNAME("432"),
    ERR_NICKNAMEINUSE("433"),
    ERR_NOTONCHANNEL("442"),
    ERR_NOTREGISTERED("451"),
    ERR_NEEDMOREPARAMS("461"),
    ERR_ALREADYREGISTERED("462");

    private final String code;

    private static final Map<String, Command> lookup = new HashMap<String, Command>() {{
        for (Command c : Command.values()) {
            put(c.code(), c);
        }
    }};

    Command(String code) {
        this.code = code;
    }

    String code() {
        return code;
    }

    public static Command fromCode(String code) {
        return lookup.get(code);
    }
}
