package choral.examples.irc;

import choral.channels.SymChannel;

public class Irc@(Client, Server) {
    private SymChannel@(Client, Server)<String> ch_AB;

    public Irc(SymChannel@(Client, Server)<String> ch_AB) {
        this.ch_AB = ch_AB;
    }

    public void run() {
        // TODO: Start clientDrivenLoop() and serverDrivenLoop() on separate threads.
    }

    private void clientDrivenLoop() {
    }

    private void serverDrivenLoop() {
    }
}
