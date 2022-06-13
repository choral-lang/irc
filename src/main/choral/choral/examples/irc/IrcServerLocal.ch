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

    public void addForwardMessageEvent(Command@R command, String@R param) {
        Message@R m = MessageBuilder@R
            .build()
            .source(Source@R.parse("irc.choral.net"@R))
            .command(Util@R.commandCode(command))
            .param(state.getNickname())
            .param(param)
            .message();

        addEvent(new ServerForwardMessageEvent@R(m));
    }

    private void addWelcome() {
        addForwardMessageEvent(Command@R.RPL_WELCOME, "Welcome to ChoralNet!"@R);
        addForwardMessageEvent(Command@R.RPL_YOURHOST, "Your host is irc.choral.net"@R);
        addForwardMessageEvent(Command@R.RPL_CREATED, "The server was created at IMADA"@R);
        addForwardMessageEvent(Command@R.RPL_MYINFO, "I'm running ChoralIRC 0.0.1"@R);
        addForwardMessageEvent(Command@R.RPL_ISUPPORT, "NICKLEN=32"@R);
        addForwardMessageEvent(Command@R.RPL_UMODEIS, "+i"@R);

        addForwardMessageEvent(Command@R.RPL_LUSERCLIENT, "There's only me and you here"@R);
        // addForwardMessageEvent(Command@R.RPL_LUSEROP, ""@R);
        // addForwardMessageEvent(Command@R.RPL_LUSERUNKNOWN, ""@R);
        // addForwardMessageEvent(Command@R.RPL_LUSERCHANNELS, ""@R);
        addForwardMessageEvent(Command@R.RPL_LUSERME, "I have exactly one user---you"@R);
        // addForwardMessageEvent(Command@R.RPL_LOCALUSERS, ""@R);
        // addForwardMessageEvent(Command@R.RPL_GLOBALUSERS, ""@R);

        addForwardMessageEvent(Command@R.RPL_MOTDSTART, "ChoralNet Message of the Day"@R);
        addForwardMessageEvent(Command@R.RPL_MOTD, "Hopefully you're having a nice day!"@R);
        addForwardMessageEvent(Command@R.RPL_MOTD, "Come find us in the office working..."@R);
        addForwardMessageEvent(Command@R.RPL_MOTD, "...or having a choco break in the lunchroom!"@R);
        addForwardMessageEvent(Command@R.RPL_ENDOFMOTD, "End of /MOTD command"@R);

        state.setWelcomeDone(true@R);
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

                        if (state.isRegistered() && !state.isWelcomeDone()) {
                            addWelcome();
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

                    if (state.isRegistered()) {
                        Message@R r = new ErrAlreadyRegisteredMessage@R(
                            state.getNickname(), "You cannot register again"@R);
                        addEvent(new ServerUserErrorEvent@R(m, r));
                    }
                    else {
                        if (Util@R.validUsername(username)) {
                            state.setUsername(username);
                            state.setRealname(realname);

                            if (state.isRegistered() && !state.isWelcomeDone()) {
                                addWelcome();
                            }
                        }
                    }
                }
            }
        }

        run();
    }
}
