package choral.examples.irc;

import java.util.List;

public class RplEndOfNamesMessage extends Message {
    public RplEndOfNamesMessage(String nickname, String channel,
                                String message) {
        super(null, Command.RPL_ENDOFNAMES.code(),
              List.of(nickname, channel, message));
    }

    public RplEndOfNamesMessage(Message message) {
        super(message);
        assert command == Command.RPL_ENDOFNAMES.code();
    }
}
