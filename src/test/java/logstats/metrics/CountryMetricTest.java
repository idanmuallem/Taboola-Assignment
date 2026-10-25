package logstats.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import logstats.GeoIpLookup;
import logstats.LogEntry;
import org.junit.jupiter.api.Test;

class CountryMetricTest {

    @Test
    void nameIsCountries() {
        assertEquals("Countries", new CountryMetric(mock(GeoIpLookup.class)).name());
    }

    @Test
    void extractPassesIpToLookupAndReturnsResult() {
        GeoIpLookup geoIp = mock(GeoIpLookup.class);
        when(geoIp.countryOf("1.1.1.1")).thenReturn("Germany");

        String value = new CountryMetric(geoIp).extract(new LogEntry("1.1.1.1", "ua"));

        assertEquals("Germany", value);
        verify(geoIp).countryOf("1.1.1.1");
    }

    @Test
    void nullLookupThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CountryMetric(null));
    }
}
