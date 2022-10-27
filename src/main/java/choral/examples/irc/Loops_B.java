package choral.examples.irc;

import choral.lang.Unit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Loops_B<T> extends LoopsImpl<T> {
    public Loops_B() {
    }

    public Unit getLoopA() {
        return Unit.id;
    }

    public LoopsLoop<T> getLoopB() {
        return new LoopsLoop<T>(queue);
    }

    public void run(Unit executorA,
                    ExecutorService executorB,
                    LoopsConsumer_B<T> stepA,
                    LoopsConsumer_A<T> stepB,
                    Unit handlerA,
                    LoopsHandler handlerB) {
        Future<?> f1 = executorB.submit(
            () -> sendLoop(t -> stepB.accept(t), handlerB));
        Future<?> f2 = executorB.submit(
            () -> recvLoop(() -> stepA.accept(), handlerB));

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

            handlerB.handleStop();
        });
    }
}
