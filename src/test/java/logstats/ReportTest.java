package logstats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import logstats.metrics.BrowserMetric;
import logstats.metrics.CountryMetric;
import logstats.metrics.Metric;
import logstats.metrics.OsMetric;
import logstats.parsers.UserAgentParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ReportTest {

    private final Locale originalLocale = Locale.getDefault();
    private final Report report = new Report();

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    /** Adds {@code count} distinct users with the given value. */
    private static void addUsers(Distribution d, String value, int count) {
        for (int i = 0; i < count; i++) {
            d.add(value + "-ip-" + i, value);
        }
    }

    private static Metric metric(String name, Distribution distribution) {
        Metric metric = mock(Metric.class);
        when(metric.name()).thenReturn(name);
        when(metric.distribution()).thenReturn(distribution);
        return metric;
    }

    @Test
    void formatsOneMetricWithTwoValuesInOrder() {
        Distribution d = new Distribution();
        addUsers(d, "Chrome", 3);
        addUsers(d, "Firefox", 1);

        String result = report.format(List.of(metric("Browser", d)));

        assertEquals("Browser:\nChrome - 75.00%\nFirefox - 25.00%", result);
    }

    @Test
    void alwaysPrintsTwoDecimals() {
        Distribution five = new Distribution();
        addUsers(five, "X", 1);
        addUsers(five, "Y", 19);
        Distribution twelveAndHalf = new Distribution();
        addUsers(twelveAndHalf, "X", 1);
        addUsers(twelveAndHalf, "Y", 7);
        Distribution hundred = new Distribution();
        addUsers(hundred, "X", 1);

        assertEquals("M:\nY - 95.00%\nX - 5.00%", report.format(List.of(metric("M", five))));
        assertEquals("M:\nY - 87.50%\nX - 12.50%", report.format(List.of(metric("M", twelveAndHalf))));
        assertEquals("M:\nX - 100.00%", report.format(List.of(metric("M", hundred))));
    }

    @Test
    void roundsToTwoDecimals() {
        Distribution d = new Distribution();
        addUsers(d, "X", 2);
        addUsers(d, "Y", 1);

        String result = report.format(List.of(metric("M", d)));

        assertEquals("M:\nX - 66.67%\nY - 33.33%", result);
    }

    @Test
    void preservesDistributionOrderDescending() {
        Distribution d = new Distribution();
        addUsers(d, "low", 1);
        addUsers(d, "high", 3);
        addUsers(d, "mid", 2);

        String result = report.format(List.of(metric("M", d)));

        assertEquals("M:\nhigh - 50.00%\nmid - 33.33%\nlow - 16.67%", result);
    }

    @Test
    void tiesFollowDistributionOrderAlphabetically() {
        Distribution d = new Distribution();
        addUsers(d, "banana", 1);
        addUsers(d, "cherry", 1);
        addUsers(d, "apple", 1);

        String result = report.format(List.of(metric("M", d)));

        assertEquals("M:\napple - 33.33%\nbanana - 33.33%\ncherry - 33.33%", result);
    }

    @Test
    void formatsAllThreeRealMetrics() {
        GeoIpLookup geoIp = mock(GeoIpLookup.class);
        when(geoIp.countryOf("1.1.1.1")).thenReturn("Germany");
        when(geoIp.countryOf("1.1.1.2")).thenReturn("Germany");
        when(geoIp.countryOf("1.1.1.3")).thenReturn("Israel");
        UserAgentParser parser = mock(UserAgentParser.class);
        when(parser.osOf("agent-1")).thenReturn("Windows");
        when(parser.osOf("agent-2")).thenReturn("Windows");
        when(parser.osOf("agent-3")).thenReturn("Android");
        when(parser.browserOf("agent-1")).thenReturn("Chrome");
        when(parser.browserOf("agent-2")).thenReturn("Firefox");
        when(parser.browserOf("agent-3")).thenReturn("Chrome");
        List<Metric> metrics = List.of(new CountryMetric(geoIp), new OsMetric(parser), new BrowserMetric(parser));
        for (LogEntry entry : List.of(
                new LogEntry("1.1.1.1", "agent-1"),
                new LogEntry("1.1.1.2", "agent-2"),
                new LogEntry("1.1.1.3", "agent-3"))) {
            metrics.forEach(m -> m.accept(entry));
        }

        String result = report.format(metrics);

        assertEquals("""
                Countries:
                Germany - 66.67%
                Israel - 33.33%

                Operating systems:
                Windows - 66.67%
                Android - 33.33%

                Browsers:
                Chrome - 66.67%
                Firefox - 33.33%""", result);
    }

    @Test
    void separatesMetricsWithExactlyOneBlankLine() {
        Distribution first = new Distribution();
        addUsers(first, "A", 1);
        Distribution second = new Distribution();
        addUsers(second, "B", 1);

        String result = report.format(List.of(metric("First", first), metric("Second", second)));

        assertEquals("First:\nA - 100.00%\n\nSecond:\nB - 100.00%", result);
    }

    @Test
    void emptyDistributionPrintsOnlyHeader() {
        String result = report.format(List.of(metric("Empty", new Distribution())));

        assertEquals("Empty:", result);
    }

    @Test
    void emptyMetricListReturnsEmptyString() {
        assertEquals("", report.format(List.of()));
    }

    @Test
    void outputIsIndependentOfDefaultLocale() {
        Locale.setDefault(Locale.GERMANY);
        Distribution d = new Distribution();
        addUsers(d, "X", 6412);
        addUsers(d, "Y", 3588);

        String result = report.format(List.of(metric("M", d)));

        assertEquals("M:\nX - 64.12%\nY - 35.88%", result);
    }

    @Test
    void nullListThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> report.format(null));
    }
}
