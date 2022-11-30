package choral.examples.irc;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class EventsImpl<T> {
    protected LinkedBlockingQueue<Optional<T>> queue;

    public EventsImpl() {
        this.queue = new LinkedBlockingQueue<Optional<T>>();
    }

    protected void sendLoop(Consumer<T> handler, LocalHandler localHandler) {
        while (true) {
            try {
                Optional<T> event = queue.take();

                if (event.isEmpty()) {
                    break;
                }

                handler.accept(event.get());
            }
            catch (InterruptedException e) {
                // Ignore
            }
            catch (Exception e) {
                if (!localHandler.onError(e)) {
                    break;
                }
            }
        }
    }

    protected void recvLoop(Runnable handler, LocalHandler localHandler) {
        while (true) {
            try {
                handler.run();
            }
            catch (Exception e) {
                if (!localHandler.onError(e)) {
                    break;
                }
            }
        }
    }
}
