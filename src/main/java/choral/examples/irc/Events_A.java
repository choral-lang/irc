package choral.examples.irc;

import choral.lang.Unit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Events_A<T> extends EventsImpl<T> {
    public Events_A() {
    }

    public EventQueue<T> queueA() {
        return new EventQueue<T>(queue);
    }

    public Unit queueB() {
        return Unit.id;
    }

    public void run(ExecutorService executorA,
                    Unit executorB,
                    EventHandler_A<T> eventHandlerA,
                    EventHandler_B<T> eventHandlerB,
                    LocalHandler localHandlerA,
                    Unit localHandlerB) {
        Future<?> f1 = executorA.submit(
            () -> sendLoop(t -> eventHandlerA.on(t), localHandlerA));
        Future<?> f2 = executorA.submit(
            () -> recvLoop(() -> eventHandlerB.on(), localHandlerA));

        executorA.execute(() -> {
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

            localHandlerA.onStop();
        });
    }
}
