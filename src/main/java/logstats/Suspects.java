package logstats;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import logstats.parsers.UserAgentParser;

/** Collects IPs whose user agent is classified as a hacking tool, for a separate report section. */
public class Suspects {

    // The value YAUAA gives to scanners and attack tools
    private static final String HACKER = "Hacker";

    private final UserAgentParser parser;
    // IP -> first suspect user agent, in the order first seen
    private final Map<String, String> suspects = new LinkedHashMap<>();

    public Suspects(UserAgentParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    /** Records the entry's IP if its user agent is a hacking tool; never affects any other statistic. */
    void accept(LogEntry entry) {
        if (HACKER.equals(parser.osOf(entry.userAgent())) || HACKER.equals(parser.browserOf(entry.userAgent()))) {
            suspects.putIfAbsent(entry.ip(), entry.userAgent());
        }
    }

    String format() {
        List<String> lines = new ArrayList<>();
        lines.add("Suspects:");
        suspects.forEach((ip, userAgent) -> lines.add(ip + " - " + userAgent));
        return String.join("\n", lines);
    }
}
