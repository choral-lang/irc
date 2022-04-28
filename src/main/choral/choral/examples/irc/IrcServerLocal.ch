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
            String@R nickname = e.getNickname();

            if (nickname == null@R) {
                Message@R m = new ErrNoNicknameGivenMessage@R();
                addEvent(new ServerNickEvent@R(nickname, m));
            }
            else {
                if (!Util@R.validNickname(nickname)) {
                    Message@R m = new ErrErroneousNicknameMessage@R();
                    addEvent(new ServerNickEvent@R(nickname, m));
                }
                else {
                    if (state.nicknameInUse(nickname)) {
                        Message@R m = new ErrNicknameInUseMessage@R();
                        addEvent(new ServerNickEvent@R(nickname, m));
                    }
                    else {
                        state.setNickname(nickname);
                    }
                }
            }
        }
        else {
            if (event.getType() == ServerLocalEventType@R.CHECK_USER) {
                ServerLocalCheckUserEvent@R e = event.asServerLocalCheckUserEvent();
                String@R username = e.getUsername();
                String@R realname = e.getRealname();

                if (username == null@R) {
                    Message@R m = new ErrNeedMoreParamsMessage@R();
                    addEvent(new ServerUserEvent@R(username, realname, null@R));
                }
                else {
                    if (state.usernameRegistered(username)) {
                        Message@R m = new ErrNeedMoreParamsMessage@R();
                        addEvent(new ServerUserEvent@R(username, realname, m));
                    }
                    else {
                        state.setUsername(username);
                        state.setRealname(realname);
                    }
                }
            }
        }

        run();
    }
}
