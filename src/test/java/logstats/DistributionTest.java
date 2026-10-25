package logstats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DistributionTest {

    private static final double DELTA = 1e-9;

    @Test
    void emptyDistributionReturnsEmptyMap() {
        assertTrue(new Distribution().percentages().isEmpty());
    }

    @Test
    void singleUserGivesHundredPercent() {
        Distribution d = new Distribution();
        d.add("1.1.1.1", "X");

        assertEquals(Map.of("X", 100.0), d.percentages());
    }

    @Test
    void sameIpWithSameValueIsCountedOnce() {
        Distribution d = new Distribution();
        d.add("1.1.1.1", "X");
        d.add("1.1.1.1", "X");

        assertEquals(Map.of("X", 100.0), d.percentages());
    }

    @Test
    void firstValueWinsForSameIp() {
        Distribution d = new Distribution();
        d.add("1.1.1.1", "X");
        d.add("1.1.1.1", "Y");

        assertEquals(Map.of("X", 100.0), d.percentages());
    }

    @Test
    void computesFullPrecisionPercentages() {
        Distribution d = new Distribution();
        d.add("A", "X");
        d.add("B", "X");
        d.add("C", "Y");

        Map<String, Double> p = d.percentages();

        assertEquals(200.0 / 3, p.get("X"), DELTA);
        assertEquals(100.0 / 3, p.get("Y"), DELTA);
    }

    @Test
    void percentagesSumToHundred() {
        Distribution d = new Distribution();
        d.add("A", "X");
        d.add("B", "X");
        d.add("C", "Y");
        d.add("D", "Z");
        d.add("E", "Z");
        d.add("F", "Z");
        d.add("G", "W");

        double sum = d.percentages().values().stream().mapToDouble(Double::doubleValue).sum();

        assertEquals(100.0, sum, DELTA);
    }

    @Test
    void sortedByPercentageDescending() {
        Distribution d = new Distribution();
        d.add("A", "low");
        d.add("B", "high");
        d.add("C", "high");
        d.add("D", "high");
        d.add("E", "mid");
        d.add("F", "mid");

        List<String> keys = new ArrayList<>(d.percentages().keySet());

        assertEquals(List.of("high", "mid", "low"), keys);
    }

    @Test
    void tiesAreOrderedAlphabetically() {
        Distribution d = new Distribution();
        d.add("A", "banana");
        d.add("B", "cherry");
        d.add("C", "apple");

        List<String> keys = new ArrayList<>(d.percentages().keySet());

        assertEquals(List.of("apple", "banana", "cherry"), keys);
    }

    @Test
    void nullIpOrValueThrowsNullPointerException() {
        Distribution d = new Distribution();

        assertThrows(NullPointerException.class, () -> d.add(null, "X"));
        assertThrows(NullPointerException.class, () -> d.add("1.1.1.1", null));
    }
}
