package logstats;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import logstats.metrics.Metric;

/** Formats metrics into a printable report. */
public class Report {

    String format(List<Metric> metrics) {
        Objects.requireNonNull(metrics);
        List<String> sections = new ArrayList<>();
        for (Metric metric : metrics) {
            sections.add(formatSection(metric));
        }
        // One blank line between sections, no trailing newline
        return String.join("\n\n", sections);
    }

    private String formatSection(Metric metric) {
        List<String> lines = new ArrayList<>();
        lines.add(metric.name() + ":");
        // Keep the order given by the distribution; rounding happens only here
        for (Map.Entry<String, Double> entry : metric.distribution().percentages().entrySet()) {
            lines.add(entry.getKey() + " - " + String.format(Locale.ROOT, "%.2f", entry.getValue()) + "%");
        }
        return String.join("\n", lines);
    }
}
