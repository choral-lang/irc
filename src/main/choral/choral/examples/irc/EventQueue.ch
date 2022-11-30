package choral.examples.irc;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue@R<T@R> {
    private LinkedBlockingQueue@R<Optional<T>> queue;

    public EventQueue(LinkedBlockingQueue@R<Optional<T>> queue) {
        this.queue = queue;
    }

    public void enqueue(T@R event) {
        Util@R.<Optional<T>>put(queue, Optional@R.<T>of(event));
    }

    public void stop() {
        Util@R.<Optional<T>>put(queue, Optional@R.<T>empty());
    }
}
