package choral.examples.irc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ForwardMessage extends Message {
    private static final Set<Command> COMMANDS = new HashSet<>(Arrays.asList(
        Command.RPL_YOURHOST,
        Command.RPL_CREATED,
        Command.RPL_MYINFO,
        Command.RPL_ISUPPORT,
        Command.RPL_UMODEIS,
        Command.RPL_LUSERCLIENT,
        Command.RPL_LUSEROP,
        Command.RPL_LUSERUNKNOWN,
        Command.RPL_LUSERCHANNELS,
        Command.RPL_LUSERME,
        Command.RPL_LOCALUSERS,
        Command.RPL_GLOBALUSERS,
        Command.RPL_ENDOFNAMES,
        Command.RPL_MOTD,
        Command.RPL_MOTDSTART,
        Command.RPL_ENDOFMOTD,
        Command.ERR_NOSUCHNICK,
        Command.ERR_NOSUCHCHANNEL,
        Command.ERR_CANNOTSENDTOCHAN,
        Command.ERR_NORECIPIENT,
        Command.ERR_NOTEXTTOSEND,
        Command.ERR_NONICKNAMEGIVEN,
        Command.ERR_ERRONEOUSNICKNAME,
        Command.ERR_NICKNAMEINUSE,
        Command.ERR_NOTONCHANNEL,
        Command.ERR_NOTREGISTERED,
        Command.ERR_NEEDMOREPARAMS,
        Command.ERR_ALREADYREGISTERED
    ));

    public ForwardMessage(Message message) {
        super(message);
        assert COMMANDS.contains(message.getCommand());
    }
}
