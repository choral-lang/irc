package choral.examples.irc;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class IrcServerLocal@R {
    private ServerState@R state;
    private long@R clientId;
    private LinkedBlockingQueue@R<ServerLocalEvent> localQueue;

    public IrcServerLocal(ServerState@R state,
                          long@R clientId) {
        this.state = state;
        this.clientId = clientId;
        this.localQueue = new LinkedBlockingQueue@R<ServerLocalEvent>();
    }

    public void addLocalEvent(ServerLocalEvent@R event) {
        Util@R.<ServerLocalEvent>put(localQueue, event);
    }

    public void addWelcomeMessage(Command@R command, String@R param) {
        Message@R m = MessageBuilder@R
            .build()
            .source(new Source@R("irc.choral.net"@R))
            .command(IrcServerLocalUtil@R.commandCode(command))
            .param(state.getNickname(clientId))
            .param(param)
            .message();

        state.addEvent(clientId, new ServerForwardMessageEvent@R(m));
    }

    private void addWelcome() {
        state.addEvent(clientId, new ServerRplWelcomeEvent@R(
            IrcServerLocalUtil@R.<RplWelcomeMessage>withSource(
                new RplWelcomeMessage@R(state.getNickname(clientId),
                                        "Welcome to ChoralNet!"@R),
                new Source@R("irc.choral.net"@R))));

        addWelcomeMessage(Command@R.RPL_YOURHOST, "Your host is irc.choral.net"@R);
        addWelcomeMessage(Command@R.RPL_CREATED, "The server was created at IMADA"@R);
        addWelcomeMessage(Command@R.RPL_MYINFO, "I'm running ChoralIRC 0.0.1"@R);
        addWelcomeMessage(Command@R.RPL_ISUPPORT, "NICKLEN=32"@R);
        addWelcomeMessage(Command@R.RPL_UMODEIS, "+i"@R);

        addWelcomeMessage(Command@R.RPL_LUSERCLIENT, "There's only me and you here"@R);
        // addWelcomeMessage(Command@R.RPL_LUSEROP, ""@R);
        // addWelcomeMessage(Command@R.RPL_LUSERUNKNOWN, ""@R);
        // addWelcomeMessage(Command@R.RPL_LUSERCHANNELS, ""@R);
        addWelcomeMessage(Command@R.RPL_LUSERME, "I have exactly one user---you"@R);
        // addWelcomeMessage(Command@R.RPL_LOCALUSERS, ""@R);
        // addWelcomeMessage(Command@R.RPL_GLOBALUSERS, ""@R);

        addWelcomeMessage(Command@R.RPL_MOTDSTART, "ChoralNet Message of the Day"@R);
        addWelcomeMessage(Command@R.RPL_MOTD, "Hopefully you're having a nice day!"@R);
        addWelcomeMessage(Command@R.RPL_MOTD, "Come find us in the office working..."@R);
        addWelcomeMessage(Command@R.RPL_MOTD, "...or having a choco break in the lunchroom!"@R);
        addWelcomeMessage(Command@R.RPL_ENDOFMOTD, "End of /MOTD command"@R);

        state.setWelcomeDone(clientId, true@R);
    }

    public void run() {
        ServerLocalEvent@R event = Util@R.<ServerLocalEvent>take(localQueue);

        if (event.getType() == ServerLocalEventType@R.CHECK_NICK) {
            ServerLocalCheckNickEvent@R e = event.asServerLocalCheckNickEvent();
            NickMessage@R m = e.getMessage();

            if (!m.hasEnoughParams()) {
                Message@R r = new ErrNoNicknameGivenMessage@R(
                    state.getNickname(clientId), "No nickname given"@R);
                state.addEvent(clientId, new ServerNickErrorEvent@R(m, r));
            }
            else {
                String@R nickname = m.getNickname();

                if (!Util@R.validNickname(nickname)) {
                    Message@R r = new ErrErroneousNicknameMessage@R(
                        state.getNickname(clientId), "Nickname is invalid"@R);
                    state.addEvent(clientId, new ServerNickErrorEvent@R(m, r));
                }
                else {
                    if (state.nicknameExists(nickname)) {
                        Message@R r = new ErrNicknameInUseMessage@R(
                            state.getNickname(clientId), "Nickname is in use"@R);
                        state.addEvent(clientId, new ServerNickErrorEvent@R(m, r));
                    }
                    else {
                        state.setNickname(clientId, nickname);

                        if (state.isRegistered(clientId)) {
                            if (!state.isWelcomeDone(clientId)) {
                                addWelcome();
                            }
                            else {
                                IrcServerLocalUtil@R.processNick(state, clientId, m);
                            }
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
                        state.getNickname(clientId), "Need at least 4 parameters!"@R);
                    state.addEvent(clientId, new ServerUserErrorEvent@R(m, r));
                }
                else {
                    String@R username = m.getUsername();
                    String@R realname = m.getRealname();

                    if (state.isRegistered(clientId)) {
                        Message@R r = new ErrAlreadyRegisteredMessage@R(
                            state.getNickname(clientId), "You cannot register again"@R);
                        state.addEvent(clientId, new ServerUserErrorEvent@R(m, r));
                    }
                    else {
                        if (Util@R.validUsername(username)) {
                            state.setUsername(clientId, username);
                            state.setRealname(clientId, realname);

                            if (state.isRegistered(clientId) && !state.isWelcomeDone(clientId)) {
                                addWelcome();
                            }
                        }
                    }
                }
            }
            else {
                if (event.getType() == ServerLocalEventType@R.JOIN) {
                    if (!state.isRegistered(clientId)) {
                        Message@R r = new ErrNotRegisteredMessage@R(
                            "unknown"@R, "You must register first!"@R);
                        state.addEvent(clientId, new ServerForwardMessageEvent@R(r));
                    }
                    else {
                        ServerLocalJoinEvent@R e = event.asServerLocalJoinEvent();
                        JoinMessage@R m = e.getMessage();

                        if (!m.hasEnoughParams()) {
                            Message@R r = new ErrNeedMoreParamsMessage@R(
                                state.getNickname(clientId), "Need at least 1 parameter!"@R);
                            state.addEvent(clientId, new ServerForwardMessageEvent@R(r));
                        }
                        else {
                            IrcServerLocalUtil@R.processJoin(state, clientId, m);
                        }
                    }
                }
                else {
                    if (event.getType() == ServerLocalEventType@R.PART) {
                        if (!state.isRegistered(clientId)) {
                            Message@R r = new ErrNotRegisteredMessage@R(
                                "unknown"@R, "You must register first!"@R);
                            state.addEvent(clientId, new ServerForwardMessageEvent@R(r));
                        }
                        else {
                            ServerLocalPartEvent@R e = event.asServerLocalPartEvent();
                            PartMessage@R m = e.getMessage();

                            if (!m.hasEnoughParams()) {
                                Message@R r = new ErrNeedMoreParamsMessage@R(
                                    state.getNickname(clientId), "Need at least 1 parameter!"@R);
                                state.addEvent(clientId, new ServerForwardMessageEvent@R(r));
                            }
                            else {
                                IrcServerLocalUtil@R.processPart(state, clientId, m);
                            }
                        }
                    }
                }
            }
        }

        run();
    }
}
