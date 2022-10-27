package choral.examples.irc;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class LoopsImpl<T> {
    protected LinkedBlockingQueue<Optional<T>> queue;

    public LoopsImpl() {
        this.queue = new LinkedBlockingQueue<Optional<T>>();
    }

    protected void sendLoop(Consumer<T> step, LoopsHandler handler) {
        while (true) {
            try {
                Optional<T> event = queue.take();

                if (event.isEmpty()) {
                    break;
                }

                step.accept(event.get());
            }
            catch (InterruptedException e) {
                // Ignore
            }
            catch (Exception e) {
                if (!handler.handleError(e)) {
                    break;
                }
            }
        }
    }

    protected void recvLoop(Runnable step, LoopsHandler handler) {
        while (true) {
            try {
                step.run();
            }
            catch (Exception e) {
                if (!handler.handleError(e)) {
                    break;
                }
            }
        }
    }
}
