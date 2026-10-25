package logstats.metrics;

import logstats.Distribution;
import logstats.LogEntry;

/** Base class for a metric that counts one attribute of each log entry. */
public abstract class Metric {

    public static final String UNKNOWN = "Unknown";

    private final Distribution distribution = new Distribution();

    public abstract String name();

    abstract String extract(LogEntry entry);

    public void accept(LogEntry entry) {
        String value = extract(entry);
        if (value == null || value.isBlank()) {
            value = UNKNOWN;
        }
        distribution.add(entry.ip(), value);
    }

    public Distribution distribution() {
        return distribution;
    }
}
