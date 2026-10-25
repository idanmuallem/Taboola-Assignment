package logstats.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import logstats.GeoIpLookup;
import logstats.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricTest {

    private GeoIpLookup geoIp;
    private Metric metric;

    @BeforeEach
    void setUp() {
        geoIp = mock(GeoIpLookup.class);
        metric = new CountryMetric(geoIp);
    }

    @Test
    void oneEntryIsCountedAtHundredPercent() {
        when(geoIp.countryOf("1.1.1.1")).thenReturn("Germany");

        metric.accept(new LogEntry("1.1.1.1", "ua"));

        assertEquals(Map.of("Germany", 100.0), metric.distribution().percentages());
    }

    @Test
    void sameIpCountsOnce() {
        when(geoIp.countryOf("1.1.1.1")).thenReturn("Germany");

        metric.accept(new LogEntry("1.1.1.1", "ua"));
        metric.accept(new LogEntry("1.1.1.1", "other"));

        assertEquals(Map.of("Germany", 100.0), metric.distribution().percentages());
    }

    @Test
    void nullLookupResultIsCountedAsUnknown() {
        when(geoIp.countryOf("1.1.1.1")).thenReturn(null);

        metric.accept(new LogEntry("1.1.1.1", "ua"));

        assertEquals(Map.of(Metric.UNKNOWN, 100.0), metric.distribution().percentages());
    }

    @Test
    void blankLookupResultIsCountedAsUnknown() {
        when(geoIp.countryOf("1.1.1.1")).thenReturn("   ");

        metric.accept(new LogEntry("1.1.1.1", "ua"));

        assertEquals(Map.of(Metric.UNKNOWN, 100.0), metric.distribution().percentages());
    }

    @Test
    void differentIpsAndCountriesSplitEvenly() {
        when(geoIp.countryOf("1.1.1.1")).thenReturn("Germany");
        when(geoIp.countryOf("2.2.2.2")).thenReturn("France");

        metric.accept(new LogEntry("1.1.1.1", "ua"));
        metric.accept(new LogEntry("2.2.2.2", "ua"));

        assertEquals(Map.of("Germany", 50.0, "France", 50.0), metric.distribution().percentages());
    }
}
