package logstats.metrics;

import java.util.Objects;
import logstats.GeoIpLookup;
import logstats.LogEntry;

/** Metric for the distribution of client countries. */
public class CountryMetric extends Metric {

    private final GeoIpLookup geoIp;

    public CountryMetric(GeoIpLookup geoIp) {
        this.geoIp = Objects.requireNonNull(geoIp);
    }

    @Override
    public String name() {
        return "Countries";
    }

    @Override
    String extract(LogEntry entry) {
        return geoIp.countryOf(entry.ip());
    }
}
