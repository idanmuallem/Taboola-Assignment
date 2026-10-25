package logstats.metrics;

import java.util.Objects;
import logstats.LogEntry;
import logstats.parsers.UserAgentParser;

/** Metric for the distribution of client operating systems. */
public class OsMetric extends Metric {

    private final UserAgentParser parser;

    public OsMetric(UserAgentParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    @Override
    public String name() {
        return "Operating systems";
    }

    @Override
    String extract(LogEntry entry) {
        return parser.osOf(entry.userAgent());
    }
}
