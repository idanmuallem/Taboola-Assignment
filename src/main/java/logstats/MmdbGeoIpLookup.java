package logstats;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

/** Resolves IP addresses to English country names using an MMDB database file. */
public class MmdbGeoIpLookup implements GeoIpLookup, Closeable {

    // Only literals are converted, so InetAddress never falls back to a DNS lookup
    private static final Pattern IPV4_LITERAL = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");

    private final DatabaseReader reader;

    public MmdbGeoIpLookup(Path mmdbFile) throws IOException {
        this.reader = new DatabaseReader.Builder(mmdbFile.toFile()).build();
    }

    @Override
    public String countryOf(String ip) {
        if (ip == null || ip.isBlank()) {
            return null;
        }
        String trimmed = ip.trim();
        if (!IPV4_LITERAL.matcher(trimmed).matches() && trimmed.indexOf(':') < 0) {
            return null;
        }
        try {
            InetAddress address = InetAddress.getByName(trimmed);
            Optional<CountryResponse> response = reader.tryCountry(address);
            return response.map(r -> r.country().name()).orElse(null);
        } catch (UnknownHostException e) {
            // Malformed literal such as 999.1.1.1
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GeoIp2Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
