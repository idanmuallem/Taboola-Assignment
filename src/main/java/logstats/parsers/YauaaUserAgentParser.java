package logstats.parsers;

import nl.basjes.parse.useragent.UserAgentAnalyzer;

/** {@link UserAgentParser} backed by YAUAA. */
public class YauaaUserAgentParser implements UserAgentParser {

    private static final String OS_FIELD = "OperatingSystemName";
    private static final String BROWSER_FIELD = "AgentName";
    private static final int CACHE_SIZE = 10_000;

    // Building the analyzer is slow: build it once and reuse it
    private final UserAgentAnalyzer analyzer;

    public YauaaUserAgentParser() {
        this.analyzer = UserAgentAnalyzer.newBuilder()
                .withField(OS_FIELD)
                .withField(BROWSER_FIELD)
                .withCache(CACHE_SIZE)
                .immediateInitialization()
                .build();
    }

    @Override
    public String osOf(String userAgent) {
        return fieldOf(userAgent, OS_FIELD);
    }

    @Override
    public String browserOf(String userAgent) {
        return fieldOf(userAgent, BROWSER_FIELD);
    }

    private String fieldOf(String userAgent, String field) {
        if (userAgent == null || userAgent.isBlank() || userAgent.equals("-")) {
            return null;
        }
        String value = analyzer.parse(userAgent).getValue(field);
        if (value == null || value.equals("Unknown") || value.equals("??")) {
            return null;
        }
        return value;
    }
}
