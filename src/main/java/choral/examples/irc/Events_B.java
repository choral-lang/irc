package choral.examples.irc;

import choral.lang.Unit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Events_B<T> extends EventsImpl<T> {
    public Events_B() {
    }

    public Unit queueA() {
        return Unit.id;
    }

    public EventQueue<T> queueB() {
        return new EventQueue<T>(queue);
    }

    public void run(Unit executorA,
                    ExecutorService executorB,
                    EventHandler_B<T> eventHandlerA,
                    EventHandler_A<T> eventHandlerB,
                    Unit localHandlerA,
                    LocalHandler localHandlerB) {
        Future<?> f1 = executorB.submit(
            () -> sendLoop(t -> eventHandlerB.on(t), localHandlerB));
        Future<?> f2 = executorB.submit(
            () -> recvLoop(() -> eventHandlerA.on(), localHandlerB));

        executorB.execute(() -> {
            try {
                f1.get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            try {
                f2.get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            localHandlerB.onStop();
        });
    }
}
