package choral.examples.irc;

import choral.lang.Unit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Loops_A<T> extends LoopsImpl<T> {
    public Loops_A() {
    }

    public LoopsLoop<T> getLoopA() {
        return new LoopsLoop<T>(queue);
    }

    public Unit getLoopB() {
        return Unit.id;
    }

    public void run(ExecutorService executorA,
                    Unit executorB,
                    LoopsConsumer_A<T> stepA,
                    LoopsConsumer_B<T> stepB,
                    LoopsHandler handlerA,
                    Unit handlerB) {
        Future<?> f1 = executorA.submit(
            () -> sendLoop(t -> stepA.accept(t), handlerA));
        Future<?> f2 = executorA.submit(
            () -> recvLoop(() -> stepB.accept(), handlerA));

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

            handlerA.handleStop();
        });
    }
}
