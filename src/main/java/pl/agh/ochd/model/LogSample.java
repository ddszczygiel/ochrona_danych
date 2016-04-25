package pl.agh.ochd.model;


import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Used in order to wrap single log line into domain object.
 */
public class LogSample {

    private final Date time;
    private final String message;

    /**
     * @param time    it is the time, when log occurred. It should be extracted from message with regex.
     * @param message the single line of logs.
     */
    public LogSample(Date time, String message) {

        if (time == null) {
            throw new IllegalArgumentException("Time cannot be null.");
        }
        if (StringUtils.isEmpty(message)) {
            throw new IllegalArgumentException("Message cannot be null or empty.");
        }

        this.time = time;
        this.message = message;
    }

    public Date getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }
}
