package logstats.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class YauaaUserAgentParserTest {

    // Building the analyzer is slow, so all tests share one instance
    private static YauaaUserAgentParser parser;

    @BeforeAll
    static void buildParser() {
        parser = new YauaaUserAgentParser();
    }

    @Test
    void nullBlankAndDashReturnNull() {
        for (String ua : new String[] {null, "", "   ", "-"}) {
            assertNull(parser.osOf(ua), "osOf: " + ua);
            assertNull(parser.browserOf(ua), "browserOf: " + ua);
        }
    }

    @Test
    void firefoxOnWindows() {
        String ua = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20100101 Firefox/15.0.1";
        assertEquals("Firefox", parser.browserOf(ua));
    }

    @Test
    void iPadSafari() {
        String ua = "Mozilla/5.0 (iPad; CPU OS 6_0_1 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) "
                + "Version/6.0 Mobile/10A523 Safari/8536.25";
        assertEquals("iOS", parser.osOf(ua));
        assertEquals("Safari", parser.browserOf(ua));
    }

    @Test
    void meaninglessStringIsReportedAsHacker() {
        // Raw YAUAA policy: unrecognisable agents are classified "Hacker"
        assertEquals("Hacker", parser.osOf("garbage"));
        assertEquals("Hacker", parser.browserOf("garbage"));
    }
}
