package logstats.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import logstats.LogEntry;
import logstats.parsers.UserAgentParser;
import org.junit.jupiter.api.Test;

class OsMetricTest {

    @Test
    void nameIsOs() {
        assertEquals("Operating systems", new OsMetric(mock(UserAgentParser.class)).name());
    }

    @Test
    void extractPassesUserAgentToParserAndReturnsResult() {
        UserAgentParser parser = mock(UserAgentParser.class);
        when(parser.osOf("some-agent")).thenReturn("Result");

        String value = new OsMetric(parser).extract(new LogEntry("1.1.1.1", "some-agent"));

        assertEquals("Result", value);
        verify(parser).osOf("some-agent");
    }

    @Test
    void nullParserThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new OsMetric(null));
    }
}
