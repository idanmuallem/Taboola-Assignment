package logstats;

/** Resolves an IP address to a country. */
public interface GeoIpLookup {

    /** Returns the English country name (e.g. "Germany", not "DE"), or null if unknown. */
    String countryOf(String ip);
}
