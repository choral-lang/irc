package choral.examples.irc;

public class IrcServerHandler@R implements LoopsHandler@R {
    private LoopsLoop@R<Message> serverLoop;
    private ServerState@R state;
    private long@R clientId;

    public IrcServerHandler(LoopsLoop@R<Message> serverLoop,
                            ServerState@R state,
                            long@R clientId) {
        this.serverLoop = serverLoop;
        this.state = state;
        this.clientId = clientId;
    }

    public boolean@R handleError(Exception@R e) {
        UnrecognizedMessageException@R ume =
            Util@R.asUnrecognizedMessageException(e);

        if (ume != null@R) {
            if (state.isRegistered(clientId)) {
                serverLoop.add(ServerUtil@R.forwardNumeric(
                    Command@R.ERR_UNKNOWNCOMMAND,
                    state.getNickname(clientId),
                    ume.getIrcMessage().getCommand(),
                    "Unknown command"@R));
            }

            return true@R;
        }

        ChannelException@R ce = Util@R.asChannelException(e);

        if (ce != null@R) {
            serverLoop.stop();

            if (!state.isQuitRequested(clientId)) {
                e.printStackTrace();
            }

            return false@R;
        }

        e.printStackTrace();

        return true@R;
    }

    public void handleStop() {
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
