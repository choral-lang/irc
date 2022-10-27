package choral.examples.irc;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

public class LoopsLoop@R<T@R> {
    private LinkedBlockingQueue@R<Optional<T>> queue;

    public LoopsLoop(LinkedBlockingQueue@R<Optional<T>> queue) {
        this.queue = queue;
    }

    public void add(T@R t) {
        Util@R.<Optional<T>>put(queue, Optional@R.<T>of(t));
    }

    public void stop() {
        Util@R.<Optional<T>>put(queue, Optional@R.<T>empty());
    }
}
