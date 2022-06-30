package choral.examples.irc;

public class ServerRplWelcomeEvent extends ServerEvent {
    private RplWelcomeMessage message;

    public ServerRplWelcomeEvent(RplWelcomeMessage message) {
        super(ServerEventType.RPL_WELCOME);
        this.message = message;
    }

    public RplWelcomeMessage getMessage() {
        return message;
    }
}
