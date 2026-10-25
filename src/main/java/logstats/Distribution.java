package logstats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Counts occurrences of values and derives percentages. */
public class Distribution {

    private final Map<String, Long> counts = new HashMap<>();
    // Total number of users is seenIps.size()
    private final Set<String> seenIps = new HashSet<>();

    public void add(String ip, String value) {
        Objects.requireNonNull(ip);
        Objects.requireNonNull(value);
        // First seen wins: ignore any later call for the same ip
        if (!seenIps.add(ip)) {
            return;
        }
        counts.merge(value, 1L, Long::sum);
    }

    public Map<String, Double> percentages() {
        Map<String, Double> result = new LinkedHashMap<>();
        if (seenIps.isEmpty()) {
            return result;
        }
        double total = seenIps.size();
        // Percentage descending, ties by value ascending
        counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .forEach(e -> result.put(e.getKey(), e.getValue() * 100.0 / total));
        return result;
    }
}
