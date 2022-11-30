package choral.examples.irc;

public class IrcServerLocalHandler@R implements LocalHandler@R {
    private EventQueue@R<Message> serverQueue;
    private ServerState@R state;
    private long@R clientId;

    public IrcServerLocalHandler(EventQueue@R<Message> serverQueue,
                                 ServerState@R state,
                                 long@R clientId) {
        this.serverQueue = serverQueue;
        this.state = state;
        this.clientId = clientId;
    }

    public boolean@R onError(Exception@R e) {
        UnrecognizedMessageException@R ume =
            Util@R.asUnrecognizedMessageException(e);

        if (ume != null@R) {
            if (state.isRegistered(clientId)) {
                serverQueue.enqueue(ServerUtil@R.forwardNumeric(
                    Command@R.ERR_UNKNOWNCOMMAND,
                    state.getNickname(clientId),
                    ume.getIrcMessage().getCommand(),
                    "Unknown command"@R));
            }

            return true@R;
        }

        ChannelException@R ce = Util@R.asChannelException(e);

        if (ce != null@R) {
            serverQueue.stop();

            if (!state.isQuitRequested(clientId)) {
                e.printStackTrace();
            }

            return false@R;
        }

        e.printStackTrace();

        return true@R;
    }

    public void onStop() {
        if (!state.isQuitRequested(clientId)) {
            ServerUtil@R.sendQuits(state, clientId,
                ServerUtil@R.<QuitMessage>withSource(
                    new QuitMessage@R("Client disconnected"@R),
                    new Source@R(ServerUtil@R.HOSTNAME)));
        }

        state.quit(clientId);
        state.getOut().println("Client disconnected"@R);
    }
}
