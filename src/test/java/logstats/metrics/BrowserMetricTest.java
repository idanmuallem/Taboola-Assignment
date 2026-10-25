package logstats.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import logstats.LogEntry;
import logstats.parsers.UserAgentParser;
import org.junit.jupiter.api.Test;

class BrowserMetricTest {

    @Test
    void nameIsBrowser() {
        assertEquals("Browsers", new BrowserMetric(mock(UserAgentParser.class)).name());
    }

    @Test
    void extractPassesUserAgentToParserAndReturnsResult() {
        UserAgentParser parser = mock(UserAgentParser.class);
        when(parser.browserOf("some-agent")).thenReturn("Result");

        String value = new BrowserMetric(parser).extract(new LogEntry("1.1.1.1", "some-agent"));

        assertEquals("Result", value);
        verify(parser).browserOf("some-agent");
    }

    @Test
    void nullParserThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BrowserMetric(null));
    }
}
