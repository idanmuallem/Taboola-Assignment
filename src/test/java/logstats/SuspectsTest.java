package logstats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import logstats.parsers.UserAgentParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SuspectsTest {

    private UserAgentParser parser;
    private Suspects suspects;

    @BeforeEach
    void setUp() {
        parser = mock(UserAgentParser.class);
        when(parser.osOf("Firefox/15")).thenReturn("Windows NT");
        when(parser.browserOf("Firefox/15")).thenReturn("Firefox");
        when(parser.osOf("ZmEu")).thenReturn("Hacker");
        when(parser.browserOf("ZmEu")).thenReturn("Hacker");
        when(parser.osOf("OnlyOs")).thenReturn("Hacker");
        when(parser.browserOf("OnlyBrowser")).thenReturn("Hacker");
        suspects = new Suspects(parser);
    }

    @Test
    void normalEntryIsNotSuspect() {
        suspects.accept(new LogEntry("1.1.1.1", "Firefox/15"));

        assertEquals("Suspects:", suspects.format());
    }

    @Test
    void unknownAgentIsNotSuspect() {
        suspects.accept(new LogEntry("1.1.1.1", "never-stubbed"));

        assertEquals("Suspects:", suspects.format());
    }

    @Test
    void hackerOsOrHackerBrowserIsSuspect() {
        suspects.accept(new LogEntry("1.1.1.1", "OnlyOs"));
        suspects.accept(new LogEntry("2.2.2.2", "OnlyBrowser"));

        assertEquals("Suspects:\n1.1.1.1 - OnlyOs\n2.2.2.2 - OnlyBrowser", suspects.format());
    }

    @Test
    void formatListsEachIpOnceWithFirstAgentInOrderSeen() {
        suspects.accept(new LogEntry("6.6.6.6", "ZmEu"));
        suspects.accept(new LogEntry("5.5.5.5", "OnlyOs"));
        suspects.accept(new LogEntry("6.6.6.6", "OnlyBrowser"));

        assertEquals("Suspects:\n6.6.6.6 - ZmEu\n5.5.5.5 - OnlyOs", suspects.format());
    }

    @Test
    void laterNormalLinesDoNotChangeASuspect() {
        suspects.accept(new LogEntry("6.6.6.6", "ZmEu"));
        suspects.accept(new LogEntry("6.6.6.6", "Firefox/15"));

        assertEquals("Suspects:\n6.6.6.6 - ZmEu", suspects.format());
    }
}
