package choral.examples.irc;

public class IrcClientHandler@R implements LoopsHandler@R {
    private LoopsLoop@R<Message> clientLoop;
    private ClientState@R state;

    public IrcClientHandler(LoopsLoop@R<Message> clientLoop,
                            ClientState@R state) {
        this.clientLoop = clientLoop;
        this.state = state;
    }

    public boolean@R handleError(Exception@R e) {
        UnrecognizedMessageException@R ume =
            Util@R.asUnrecognizedMessageException(e);

        if (ume != null@R) {
            state.getOut().println(ume.getMessage());
            return true@R;
        }

        ChannelException@R ce = Util@R.asChannelException(e);

        if (ce != null@R) {
            clientLoop.stop();

            if (!state.isGracefulQuit()) {
                e.printStackTrace();
            }

            return false@R;
        }

        e.printStackTrace();

        return true@R;
    }

    public void handleStop() {}
}
