package logstats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class MmdbGeoIpLookupTest {

    private MmdbGeoIpLookup lookup;

    @AfterEach
    void closeLookup() throws IOException {
        if (lookup != null) {
            lookup.close();
        }
    }

    /** Finds a .mmdb file in the project root (the working directory of the build), or skips the test. */
    private static Path findDatabase() throws IOException {
        try (Stream<Path> files = Files.list(Path.of("").toAbsolutePath())) {
            Path db = files.filter(p -> p.getFileName().toString().endsWith(".mmdb")).findFirst().orElse(null);
            Assumptions.assumeTrue(db != null && Files.exists(db), "no .mmdb file in the project root");
            return db;
        }
    }

    private String countryOf(String ip) throws IOException {
        lookup = new MmdbGeoIpLookup(findDatabase());
        return lookup.countryOf(ip);
    }

    @Test
    void resolvesIsraeliAddress() throws IOException {
        assertEquals("Israel", countryOf("82.166.148.154"));
    }

    @Test
    void resolvesUnitedStatesAddress() throws IOException {
        assertEquals("United States", countryOf("65.34.248.51"));
    }

    @Test
    void resolvesSecondUnitedStatesAddress() throws IOException {
        assertEquals("United States", countryOf("66.249.75.75"));
    }

    @Test
    void resolvesChineseAddress() throws IOException {
        assertEquals("China", countryOf("180.76.5.154"));
    }

    @Test
    void privateAddressReturnsNull() throws IOException {
        assertNull(countryOf("192.168.1.1"));
    }

    @Test
    void invalidInputReturnsNullWithoutNetworkAccess() throws IOException {
        lookup = new MmdbGeoIpLookup(findDatabase());

        assertNull(lookup.countryOf("not-an-ip"));
        assertNull(lookup.countryOf(""));
        assertNull(lookup.countryOf(null));
    }

    @Test
    void missingFileThrowsIOException() {
        assertThrows(IOException.class, () -> new MmdbGeoIpLookup(Path.of("does-not-exist.mmdb")));
    }
}
