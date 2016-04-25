package pl.agh.ochd.infrastructure;


import pl.agh.ochd.model.LogSample;
import pl.agh.ochd.model.ResourceId;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementation of this is responsible of storing and loading logs.
 */
public interface PersistenceService {

    /**
     * Use in order to store new logs.
     *
     * @param id   monitored resource id
     * @param logs collection of log samples to store
     */
    void saveLogs(ResourceId id, Collection<LogSample> logs);

    /**
     * Use in order to load logs within specified time. If there is no pattern, all samples will be returned.
     *
     * @param id      monitored resource id
     * @param from    the begin of time range
     * @param to      the end of time range
     * @param pattern optional pattern to match
     * @return returns collection of samples
     */
    Collection<LogSample> loadLogs(ResourceId id, Date from, Date to, Optional<Pattern> pattern);

}
