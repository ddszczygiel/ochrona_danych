package pl.agh.ochd.model;


import java.util.Collection;

public class NotificationData {

    private String hostName;
    private String patternName;
    private Collection<LogSample> matched;

    public NotificationData(String hostName, String patternName, Collection<LogSample> matched) {
        this.hostName = hostName;
        this.patternName = patternName;
        this.matched = matched;
    }

    public String getHostName() {
        return hostName;
    }

    public String getPatternName() {
        return patternName;
    }

    public Collection<LogSample> getMatched() {
        return matched;
    }
}
