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
        try {
            queue.put(event);
        }
        catch (InterruptedException@R e) {
            // Ignore the interrupt and try again.
            addEvent(event);
        }
    }

    public void addLocalEvent(ClientLocalEvent@R event) {
        try {
            localQueue.put(event);
        }
        catch (InterruptedException@R e) {
            // Ignore the interrupt and try again.
            addLocalEvent(event);
        }
    }

    private ClientLocalEvent@R takeLocalEvent() {
        try {
            return localQueue.take();
        }
        catch (InterruptedException@R e) {
            // Ignore the interrupt and try again.
            return takeLocalEvent();
        }
    }

    public void run() {
        ClientLocalEvent@R event = takeLocalEvent();

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

                    if (source.getNickname().equals(state.getNickname()) &&
                        channels.size() == 1@R) {
                        state.joinChannel(channels.get(0@R));
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

                        if (source.getNickname().equals(state.getNickname()) &&
                            channels.size() == 1@R) {
                            state.partChannel(channels.get(0@R));
                        }
                    }
                }
            }
        }

        run();
    }
}
