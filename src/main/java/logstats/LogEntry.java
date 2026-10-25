package logstats;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A parsed log line: client IP and user agent. */
public record LogEntry(String ip, String userAgent) {

    // Not anchored at the end: extra fields may follow the user agent
    private static final Pattern LINE = Pattern.compile(
            "^(?<ip>\\S+) \\S+ \\S+ \\[[^\\]]+\\] \"[^\"]*\" \\d{3} \\S+ \"[^\"]*\" \"(?<agent>[^\"]*)\"");

    static Optional<LogEntry> parse(String line) {
        if (line == null) {
            return Optional.empty();
        }
        Matcher m = LINE.matcher(line);
        if (!m.find()) {
            return Optional.empty();
        }
        return Optional.of(new LogEntry(m.group("ip"), m.group("agent")));
    }
}
