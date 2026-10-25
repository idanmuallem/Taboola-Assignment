package logstats.metrics;

import java.util.Objects;
import logstats.LogEntry;
import logstats.parsers.UserAgentParser;

/** Metric for the distribution of client browsers. */
public class BrowserMetric extends Metric {

    private final UserAgentParser parser;

    public BrowserMetric(UserAgentParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    @Override
    public String name() {
        return "Browsers";
    }

    @Override
    String extract(LogEntry entry) {
        return parser.browserOf(entry.userAgent());
    }
}
