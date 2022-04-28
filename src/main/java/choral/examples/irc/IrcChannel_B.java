package choral.examples.irc;

import java.nio.channels.ByteChannel;
import choral.channels.SymChannel_B;

public class IrcChannel_B extends IrcChannelImpl implements SymChannel_B<Message> {
    public IrcChannel_B(ByteChannel channel) {
        super(channel);
    }
}
