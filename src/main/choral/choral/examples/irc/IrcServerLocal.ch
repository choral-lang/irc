package choral.examples.irc;

import java.util.concurrent.LinkedBlockingQueue;

public class IrcServerLocal@R {
    private ServerState@R state;
    private LinkedBlockingQueue@R<ServerEvent> queue;
    private LinkedBlockingQueue@R<ServerLocalEvent> localQueue;

    public IrcServerLocal(ServerState@R state,
                          LinkedBlockingQueue@R<ServerEvent> queue) {
        this.state = state;
        this.queue = queue;
        this.localQueue = new LinkedBlockingQueue@R<ServerLocalEvent>();
    }

    private Integer@R addEvent(ServerEvent@R event) {
        try {
            queue.put(event);
            return null@R;
        }
        catch (InterruptedException@R e) {
            // Ignore the interrupt and try again.
            return addEvent(event);
        }
    }

    public Integer@R addLocalEvent(ServerLocalEvent@R event) {
        try {
            localQueue.put(event);
            return null@R;
        }
        catch (InterruptedException@R e) {
            // Ignore the interrupt and try again.
            return addLocalEvent(event);
        }
    }

    private ServerLocalEvent@R takeLocalEvent() {
        try {
            return localQueue.take();
        }
        catch (InterruptedException@R e) {
            // Ignore the interrupt and try again.
            return takeLocalEvent();
        }
    }

    public void run() {
        ServerLocalEvent@R event = takeLocalEvent();

        if (event.getType() == ServerLocalEventType@R.CHECK_NICK) {
            ServerLocalCheckNickEvent@R e = event.asServerLocalCheckNickEvent();
            NickMessage@R m = e.getMessage();

            if (!m.hasEnoughParams()) {
                Message@R r = new ErrNoNicknameGivenMessage@R(
                    state.getNickname(), "No nickname given"@R);
                addEvent(new ServerNickErrorEvent@R(m, r));
            }
            else {
                String@R nickname = m.getNickname();

                if (!Util@R.validNickname(nickname)) {
                    Message@R r = new ErrErroneousNicknameMessage@R(
                        state.getNickname(), "Nickname is invalid"@R);
                    addEvent(new ServerNickErrorEvent@R(m, r));
                }
                else {
                    if (state.nicknameInUse(nickname)) {
                        Message@R r = new ErrNicknameInUseMessage@R(
                            state.getNickname(), "Nickname is in use"@R);
                        addEvent(new ServerNickErrorEvent@R(m, r));
                    }
                    else {
                        state.setNickname(nickname);

                        if (state.isRegistrationDone()) {
                            addEvent(new ServerRegistrationCompleteEvent@R());
                        }
                    }
                }
            }
        }
        else {
            if (event.getType() == ServerLocalEventType@R.CHECK_USER) {
                ServerLocalCheckUserEvent@R e = event.asServerLocalCheckUserEvent();
                UserMessage@R m = e.getMessage();

                if (!m.hasEnoughParams()) {
                    Message@R r = new ErrNeedMoreParamsMessage@R(
                        state.getNickname(), "Need at least 4 parameters!"@R);
                    addEvent(new ServerUserErrorEvent@R(m, r));
                }
                else {
                    String@R username = m.getUsername();
                    String@R realname = m.getRealname();

                    if (state.usernameRegistered(username)) {
                        Message@R r = new ErrAlreadyRegisteredMessage@R(
                            state.getNickname(), "Username is already registered"@R);
                        addEvent(new ServerUserErrorEvent@R(m, r));
                    }
                    else {
                        state.setUsername(username);
                        state.setRealname(realname);

                        if (state.isRegistrationDone()) {
                            addEvent(new ServerRegistrationCompleteEvent@R());
                        }
                    }
                }
            }
        }

        run();
    }
}
