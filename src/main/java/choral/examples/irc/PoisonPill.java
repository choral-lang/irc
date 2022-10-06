package choral.examples.irc;

import java.util.List;

public class PoisonPill extends Message {
    public PoisonPill() {
        super(null, "POISON", List.of());
    }
}
