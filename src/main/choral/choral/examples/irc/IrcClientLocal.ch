package choral.examples.irc;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class IrcClientLocal@R {
    private ClientState@R state;
    private LinkedBlockingQueue@R<ClientEvent> queue;
    private LinkedBlockingQueue@R<ClientLocalEvent> localQueue;

    public IrcClientLocal(ClientState@R state,
                          LinkedBlockingQueue@R<ClientEvent> queue) {
        this.state = state;
        this.queue = queue;
        this.localQueue = new LinkedBlockingQueue@R<ClientLocalEvent>();
    }

    private void addEvent(ClientEvent@R event) {
        Util@R.<ClientEvent>put(queue, event);
    }

    public void addLocalEvent(ClientLocalEvent@R event) {
        Util@R.<ClientLocalEvent>put(localQueue, event);
    }

    public void run() {
        ClientLocalEvent@R event = Util@R.<ClientLocalEvent>take(localQueue);

        if (event.getType() == ClientLocalEventType@R.PONG) {
            ClientLocalPongEvent@R e = event.asClientLocalPongEvent();
            PingMessage@R m = e.getMessage();

            if (m.hasEnoughParams()) {
                addEvent(new ClientPongEvent@R(m.getToken()));
            }
        }
        else {
            if (event.getType() == ClientLocalEventType@R.JOIN) {
                ClientLocalJoinEvent@R e = event.asClientLocalJoinEvent();
                JoinMessage@R m = e.getMessage();
                Source@R source = m.getSource();

                if (source != null@R && m.hasEnoughParams()) {
                    List@R<String> channels = m.getChannels();
                    String@R nickname = source.getNickname();
                    // NOTE: We expect just a single channel, so ignore the others, if any
                    String@R channel = channels.get(0@R);

                    if (nickname.equals(state.getNickname())) {
                        if (!state.inChannel(channel)) {
                            state.joinChannel(channel);
                        }
                    }
                    else {
                        if (state.inChannel(channel)) {
                            state.addMember(channel, nickname);
                        }
                    }
                }
            }
            else {
                if (event.getType() == ClientLocalEventType@R.PART) {
                    ClientLocalPartEvent@R e = event.asClientLocalPartEvent();
                    PartMessage@R m = e.getMessage();
                    Source@R source = m.getSource();

                    if (source != null@R && m.hasEnoughParams()) {
                        List@R<String> channels = m.getChannels();
                        String@R nickname = source.getNickname();
                        // NOTE: We expect just a single channel, so ignore the others, if any
                        String@R channel = channels.get(0@R);

                        if (nickname.equals(state.getNickname())) {
                            if (state.inChannel(channel)) {
                                state.partChannel(channel);
                            }
                        }
                        else {
                            if (state.inChannel(channel)) {
                                state.removeMember(channel, nickname);
                            }
                        }
                    }
                }
            }
        }

        run();
    }
}
