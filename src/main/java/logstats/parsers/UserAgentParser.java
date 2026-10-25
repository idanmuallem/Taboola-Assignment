package logstats.parsers;

/** Extracts OS and browser names from a user-agent string. */
public interface UserAgentParser {

    /** Returns the OS name, or null if unknown. */
    String osOf(String userAgent);

    /** Returns the browser name, or null if unknown. */
    String browserOf(String userAgent);
}
