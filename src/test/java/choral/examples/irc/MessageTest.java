package choral.examples.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MessageTest {
    private static final List<String> EXAMPLES =
        List.of(
            "example-libera.txt",
            // Taken from https://modern.ircdocs.horse/.
            "example-ircdocs.txt");

    @Test
    public void testEmpty() {
        assertNull(Message.parse(""));
    }

    @Test
    public void testCommand() {
        Message m = Message.parse("CMD");
        assertNotNull(m);
        assertNull(m.getSource());
        assertEquals("CMD", m.getCommand());
        assertEquals(0, m.getParams().size());
    }

    @Test
    public void testSource() {
        Message m = Message.parse(":source CMD");
        assertNotNull(m.getSource());
        assertEquals("source", m.getSource().toString());
        assertEquals("CMD", m.getCommand());
        assertEquals(0, m.getParams().size());
    }

    @Test
    public void testParams() {
        Message m = Message.parse("CMD #param1 param2");
        assertNotNull(m);
        assertNull(m.getSource());
        assertEquals("CMD", m.getCommand());
        assertEquals(List.of("#param1", "param2"), m.getParams());
    }

    @Test
    public void testTrailingParam() {
        Message m = Message.parse("CMD :#param1 param2");
        assertNotNull(m);
        assertNull(m.getSource());
        assertEquals("CMD", m.getCommand());
        assertEquals(List.of("#param1 param2"), m.getParams());
    }

    @Test
    public void testEmptyTrailingParam() {
        Message m = Message.parse("CMD :");
        assertNotNull(m);
        assertNull(m.getSource());
        assertEquals("CMD", m.getCommand());
        assertEquals(List.of(""), m.getParams());
    }

    private static Stream<String> resourceLines(String resource)
        throws IOException, URISyntaxException {
        ClassLoader loader = MessageTest.class.getClassLoader();
        return Files.lines(Paths.get(loader.getResource(resource).toURI()));
    }

    private static Stream<String> examples() {
        return EXAMPLES.stream().flatMap(r -> {
                    try {
                        return resourceLines(r);
                    }
                    catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                        return Stream.empty();
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("examples")
    public void testCanParse(String message) {
        assertNotNull(Message.parse(message), message);
    }

    @ParameterizedTest
    @MethodSource("examples")
    public void testRoundtrip(String message) {
        Message m = Message.parse(message);
        assertNotNull(m, message);

        String message2 = m.toString();
        Message m2 = Message.parse(message2);
        assertNotNull(m2, message2);

        assertEquals(message2, m2.toString());
    }
}
