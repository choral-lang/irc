package choral.examples.irc;

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

    private Integer@R addEvent(ClientEvent@R event) {
        try {
            queue.put(event);
            return null@R;
        }
        catch (InterruptedException@R e) {
            // Ignore the interrupt and try again.
            return addEvent(event);
        }
    }

    public Integer@R addLocalEvent(ClientLocalEvent@R event) {
        try {
            localQueue.put(event);
            return null@R;
        }
        catch (InterruptedException@R e) {
            // Ignore the interrupt and try again.
            return addLocalEvent(event);
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

        run();
    }
}
