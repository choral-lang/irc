package choral.examples.irc;

public class IrcClientLocalHandler@R implements LocalHandler@R {
    private EventQueue@R<Message> clientQueue;
    private ClientState@R state;

    public IrcClientLocalHandler(EventQueue@R<Message> clientQueue,
                                 ClientState@R state) {
        this.clientQueue = clientQueue;
        this.state = state;
    }

    public boolean@R onError(Exception@R e) {
        UnrecognizedMessageException@R ume =
            Util@R.asUnrecognizedMessageException(e);

        if (ume != null@R) {
            state.getOut().println(ume.getMessage());
            return true@R;
        }

        ChannelException@R ce = Util@R.asChannelException(e);

        if (ce != null@R) {
            clientQueue.stop();

            if (!state.isQuitRequested()) {
                e.printStackTrace();
            }

            return false@R;
        }

        e.printStackTrace();

        return true@R;
    }

    public void onStop() {}
}
