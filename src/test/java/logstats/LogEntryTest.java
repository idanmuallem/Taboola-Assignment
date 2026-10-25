package logstats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class LogEntryTest {

    private static final String IPAD_AGENT =
            "Mozilla/5.0 (iPad; CPU OS 6_0_1 like Mac OS X) AppleWebKit/536.26 "
                    + "(KHTML, like Gecko) Version/6.0 Mobile/10A523 Safari/8536.25";

    @Test
    void parsesNormalLine() {
        String line = "65.34.248.51 - - [20/Jan/2013:04:33:29 -0600] "
                + "\"GET /?utm_source=Contextin HTTP/1.1\" 200 9983 \"-\" \""
                + IPAD_AGENT + "\" 625 10246 - 233945";

        assertEquals(Optional.of(new LogEntry("65.34.248.51", IPAD_AGENT)), LogEntry.parse(line));
    }

    @Test
    void parsesPostLineWithDashBytes() {
        String line = "50.97.37.217 - - [20/Jan/2013:05:10:21 -0600] "
                + "\"POST /wp-cron.php?doing_wp_cron=1358680221.2960379123687744140625 HTTP/1.0\" "
                + "200 - \"-\" \"WordPress/3.5; http://50.97.37.217\" 275 190 - 517683";

        assertEquals(
                Optional.of(new LogEntry("50.97.37.217", "WordPress/3.5; http://50.97.37.217")),
                LogEntry.parse(line));
    }

    @Test
    void keepsDashUserAgentAsIs() {
        String line = "1.2.3.4 - - [20/Jan/2013:04:33:29 -0600] \"GET / HTTP/1.1\" 200 10 \"-\" \"-\"";

        assertEquals(Optional.of(new LogEntry("1.2.3.4", "-")), LogEntry.parse(line));
    }

    @Test
    void parsesLineWithReferrerUrl() {
        String line = "1.2.3.4 - - [20/Jan/2013:04:33:29 -0600] \"GET / HTTP/1.1\" 200 10 "
                + "\"http://example.com/page?a=b\" \"Mozilla/5.0 (X11; Linux x86_64)\"";

        assertEquals(
                Optional.of(new LogEntry("1.2.3.4", "Mozilla/5.0 (X11; Linux x86_64)")),
                LogEntry.parse(line));
    }

    @Test
    void ignoresExtraTrailingFields() {
        String line = "1.2.3.4 - - [20/Jan/2013:04:33:29 -0600] \"GET / HTTP/1.1\" 200 10 \"-\" "
                + "\"Agent/1.0\" 625 10246 - 233945 extra \"quoted extra\"";

        assertEquals(Optional.of(new LogEntry("1.2.3.4", "Agent/1.0")), LogEntry.parse(line));
    }

    @Test
    void returnsEmptyForNullEmptyBlankAndGarbage() {
        assertTrue(LogEntry.parse(null).isEmpty());
        assertTrue(LogEntry.parse("").isEmpty());
        assertTrue(LogEntry.parse("   \t ").isEmpty());
        assertTrue(LogEntry.parse("this is not a log line").isEmpty());
    }
}
