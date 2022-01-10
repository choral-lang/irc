package choral.examples.irc;

import java.nio.channels.ByteChannel;
import choral.channels.SymChannel_A;

public class IrcChannel_A extends IrcChannelImpl implements SymChannel_A<Message> {
    public IrcChannel_A(ByteChannel channel) {
        super(channel);
    }
}
