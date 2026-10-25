package logstats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import logstats.metrics.BrowserMetric;
import logstats.metrics.CountryMetric;
import logstats.metrics.Metric;
import logstats.metrics.OsMetric;
import logstats.parsers.UserAgentParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainTest {

    private static final String WIN_UA =
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20100101 Firefox/15.0.1";
    private static final String MAC_UA =
            "Mozilla/5.0 (iPad; CPU OS 6_0_1 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) "
                    + "Version/6.0 Mobile/10A523 Safari/8536.25";

    private GeoIpLookup geoIp;
    private UserAgentParser parser;

    @BeforeEach
    void setUp() {
        geoIp = mock(GeoIpLookup.class);
        when(geoIp.countryOf("1.1.1.1")).thenReturn("Germany");
        when(geoIp.countryOf("2.2.2.2")).thenReturn("Germany");
        when(geoIp.countryOf("3.3.3.3")).thenReturn("France");

        parser = mock(UserAgentParser.class);
        when(parser.osOf(WIN_UA)).thenReturn("Windows NT");
        when(parser.browserOf(WIN_UA)).thenReturn("Firefox");
        when(parser.osOf(MAC_UA)).thenReturn("Mac OS");
        when(parser.browserOf(MAC_UA)).thenReturn("Safari");
    }

    private List<Metric> allMetrics() {
        return List.of(new CountryMetric(geoIp), new OsMetric(parser), new BrowserMetric(parser));
    }

    private static String line(String ip, String userAgent) {
        return ip + " - - [20/Jan/2013:04:33:29 -0600] \"GET / HTTP/1.1\" 200 9983 \"-\" \""
                + userAgent + "\" 625 10246 - 233945";
    }

    private static final String EXPECTED_REPORT = """
            Countries:
            Germany - 66.67%
            France - 33.33%

            Operating systems:
            Windows NT - 66.67%
            Mac OS - 33.33%

            Browsers:
            Firefox - 66.67%
            Safari - 33.33%""";

    @Test
    void threeValidLinesProduceFullReport() {
        Stream<String> lines = Stream.of(line("1.1.1.1", WIN_UA), line("2.2.2.2", WIN_UA), line("3.3.3.3", MAC_UA));

        assertEquals(EXPECTED_REPORT, Main.analyze(lines, allMetrics()));
    }

    @Test
    void malformedLinesAreSkipped() {
        Stream<String> lines = Stream.of(
                "this is not a log line",
                line("1.1.1.1", WIN_UA),
                "",
                line("2.2.2.2", WIN_UA),
                "1.2.3.4 - - broken",
                line("3.3.3.3", MAC_UA));

        assertEquals(EXPECTED_REPORT, Main.analyze(lines, allMetrics()));
    }

    @Test
    void emptyStreamProducesOnlyHeaders() {
        assertEquals("Countries:\n\nOperating systems:\n\nBrowsers:",
                Main.analyze(Stream.empty(), allMetrics()));
    }

    @Test
    void reportWithoutCountryMetricHasNoCountriesSection() {
        List<Metric> metrics = List.of(new OsMetric(parser), new BrowserMetric(parser));

        String report = Main.analyze(Stream.of(line("1.1.1.1", WIN_UA)), metrics);

        assertFalse(report.contains("Countries:"));
        assertEquals("Operating systems:\nWindows NT - 100.00%\n\nBrowsers:\nFirefox - 100.00%", report);
    }

    @Test
    void sameIpIsCountedOnceFirstLineWins() {
        Stream<String> lines = Stream.of(line("1.1.1.1", WIN_UA), line("1.1.1.1", MAC_UA));

        String report = Main.analyze(lines, List.of(new OsMetric(parser)));

        assertEquals("Operating systems:\nWindows NT - 100.00%", report);
    }

    @Test
    void suspectsAreListedButStillCountedInDistributions() {
        when(parser.osOf("ZmEu")).thenReturn("Hacker");
        when(parser.browserOf("ZmEu")).thenReturn("Hacker");
        Stream<String> lines = Stream.of(
                line("1.1.1.1", WIN_UA), line("6.6.6.6", "ZmEu"), line("3.3.3.3", MAC_UA),
                line("6.6.6.6", WIN_UA));

        String report = Main.analyze(lines, List.of(new OsMetric(parser), new BrowserMetric(parser)),
                new Suspects(parser));

        assertEquals("""
                Operating systems:
                Hacker - 33.33%
                Mac OS - 33.33%
                Windows NT - 33.33%

                Browsers:
                Firefox - 33.33%
                Hacker - 33.33%
                Safari - 33.33%

                Suspects:
                6.6.6.6 - ZmEu""", report);
    }

    @Test
    void suspectsDoNotChangeTheDistributions() {
        when(parser.osOf("ZmEu")).thenReturn("Hacker");
        when(parser.browserOf("ZmEu")).thenReturn("Hacker");
        List<String> lines = List.of(
                line("1.1.1.1", WIN_UA), line("6.6.6.6", "ZmEu"), line("3.3.3.3", MAC_UA),
                line("6.6.6.6", WIN_UA), line("2.2.2.2", WIN_UA));

        String plain = Main.analyze(lines.stream(), allMetrics());
        String withSuspects = Main.analyze(lines.stream(), allMetrics(), new Suspects(parser));

        assertEquals(plain + "\n\nSuspects:\n6.6.6.6 - ZmEu", withSuspects);
    }

    @Test
    void ipNormalFirstThenHackerLaterIsCountedAndListed() {
        when(parser.osOf("ZmEu")).thenReturn("Hacker");
        when(parser.browserOf("ZmEu")).thenReturn("Hacker");
        Stream<String> lines = Stream.of(line("1.1.1.1", WIN_UA), line("1.1.1.1", "ZmEu"));

        String report = Main.analyze(lines, List.of(new OsMetric(parser), new BrowserMetric(parser)),
                new Suspects(parser));

        assertEquals("""
                Operating systems:
                Windows NT - 100.00%

                Browsers:
                Firefox - 100.00%

                Suspects:
                1.1.1.1 - ZmEu""", report);
    }
}
