package logstats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import logstats.metrics.BrowserMetric;
import logstats.metrics.CountryMetric;
import logstats.metrics.Metric;
import logstats.metrics.OsMetric;
import logstats.parsers.UserAgentParser;
import logstats.parsers.YauaaUserAgentParser;

/** Program entry point. */
public class Main {

    static String analyze(Stream<String> lines, List<Metric> metrics) {
        lines.forEach(line -> LogEntry.parse(line)
                .ifPresent(entry -> metrics.forEach(m -> m.accept(entry))));
        return new Report().format(metrics);
    }

    /** Like {@link #analyze(Stream, List)}, plus a Suspects section; suspects are observed, never excluded. */
    static String analyze(Stream<String> lines, List<Metric> metrics, Suspects suspects) {
        lines.forEach(line -> LogEntry.parse(line).ifPresent(entry -> {
            suspects.accept(entry);
            metrics.forEach(m -> m.accept(entry));
        }));
        return new Report().format(metrics) + "\n\n" + suspects.format();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: Main <apache.log path> [<country .mmdb path>]");
            System.exit(1);
        }
        UserAgentParser parser = new YauaaUserAgentParser();
        // The lookup may be null; try-with-resources skips closing a null resource
        try (MmdbGeoIpLookup geoIp = openGeoIp(args);
                Stream<String> lines = Files.lines(Path.of(args[0]))) {
            List<Metric> metrics = new ArrayList<>();
            if (geoIp != null) {
                metrics.add(new CountryMetric(geoIp));
            }
            metrics.add(new OsMetric(parser));
            metrics.add(new BrowserMetric(parser));
            System.out.println(analyze(lines, metrics, new Suspects(parser)));
        }
    }

    /** Opens the GeoIP database if possible; GeoIP problems must never crash the program. */
    private static MmdbGeoIpLookup openGeoIp(String[] args) {
        if (args.length < 2) {
            System.err.println("Country metric skipped: no .mmdb file given");
            return null;
        }
        try {
            return new MmdbGeoIpLookup(Path.of(args[1]));
        } catch (IOException | RuntimeException e) {
            System.err.println("Country metric skipped: " + e);
            return null;
        }
    }
}
