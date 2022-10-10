package choral.examples.irc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class SourceTest {
    @Test
    public void testEmpty() {
        Source s = Source.parse("");
        assertNotNull(s);
        assertEquals("", s.getNickname());
        assertNull(s.getUsername());
        assertNull(s.getHostname());
    }

    @Test
    public void testNickname() {
        Source s = Source.parse("nick");
        assertNotNull(s);
        assertEquals("nick", s.getNickname());
        assertNull(s.getUsername());
        assertNull(s.getHostname());
    }

    @Test
    public void testUsername() {
        Source s = Source.parse("nick!user");
        assertNotNull(s);
        assertEquals("nick", s.getNickname());
        assertEquals("user", s.getUsername());
        assertNull(s.getHostname());
    }

    @Test
    public void testNicknameUsernameHostname() {
        Source s = Source.parse("nick!user@host");
        assertNotNull(s);
        assertEquals("nick", s.getNickname());
        assertEquals("user", s.getUsername());
        assertEquals("host", s.getHostname());
    }

    @Test
    public void testNicknameHostname() {
        Source s = Source.parse("nick@host");
        assertNotNull(s);
        assertEquals("nick", s.getNickname());
        assertNull(s.getUsername());
        assertEquals("host", s.getHostname());
    }

    @Test
    public void testWhitespace1() {
        Source s = Source.parse(" ");
        assertNotNull(s);
        assertEquals(" ", s.getNickname());
        assertNull(s.getUsername());
        assertNull(s.getHostname());
    }

    @Test
    public void testWhitespace2() {
        Source s = Source.parse(" ! @ ");
        assertNotNull(s);
        assertEquals(" ", s.getNickname());
        assertEquals(" ", s.getUsername());
        assertEquals(" ", s.getHostname());
    }
}
