package choral.examples.irc;

public class ServerRegistrationCompleteEvent extends ServerEvent {
    public ServerRegistrationCompleteEvent() {
        super(ServerEventType.REGISTRATION_COMPLETE);
    }
}
