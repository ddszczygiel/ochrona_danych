package pl.agh.ochd.execution;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.agh.ochd.connectors.Connector;
import pl.agh.ochd.connectors.ConnectorFactory;
import pl.agh.ochd.model.*;
import pl.agh.ochd.logs.LogHelper;
import pl.agh.ochd.infrastructure.PersistenceService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class AnalyzerWorker implements Callable<Collection<NotificationData>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerWorker.class);
    private static final Date FUTURE_DATE = Date.from(Instant.now().plus(48*3600, ChronoUnit.SECONDS));

    private RemoteHost host;
    private Connector connector;
    private PersistenceService service;
    private LogHelper helper;
    private ResourceId resourceId;


    public AnalyzerWorker(RemoteHost host, PersistenceService service) {

        this.connector = ConnectorFactory.getConnector(host);
        this.host = host;
        this.service = service;
        this.resourceId = new ResourceId(host.getHostName());
        this.helper = new LogHelper();
    }

    @Override
    public Collection<NotificationData> call() throws Exception {

        // get logs
        LOGGER.debug("Starting log worker");
        Optional<List<String>> logs = connector.getLogs();
        if (!logs.isPresent()) {
            // something goes wrong
            LOGGER.error("Could not retrieve logs from host: " + host.getHostName());
            return Collections.emptyList();
        }

        List<String> lines = logs.get();
        LOGGER.debug("Retrieved logs count: " + lines.size());
        LOGGER.debug("Pushing logs to db");
        service.saveLogs(resourceId, helper.convertToDomainModel(lines, host.getLogDateFormat(), host.getLogDatePattern()));
        Date lastReceivedLogDate = helper.getLastReceivedLogDate(lines.get(lines.size() - 1), host.getLogDateFormat(), host.getLogDatePattern());

        TimeUnit.SECONDS.sleep(10);
        LOGGER.debug("Analyzing patterns...");
        List<NotificationData> patternsMatched = analyzePatterns();
        LOGGER.debug("Analyzing sequences...");
        List<NotificationData> sequenceMatched = analyzeSequences();
        LOGGER.debug("Analyzing time sequences...");
        List<NotificationData> timeSequencesMatched = analyzeTimeSequences();

        List<NotificationData> allMatches = new ArrayList<>(patternsMatched);
        allMatches.addAll(sequenceMatched);
        allMatches.addAll(timeSequencesMatched);
        LOGGER.debug("Notifications count: " + allMatches.size());

        // TODO store it somehow
        host.setLastReceivedLogDate(lastReceivedLogDate);
        LOGGER.debug("Last received log date: " + lastReceivedLogDate);
        LOGGER.debug("Finished log retrieving for host: " + host.getHostName());
        return allMatches;
    }

    private List<NotificationData> analyzePatterns() {

        List<NotificationData> notifications = new ArrayList<>();
        for (Map.Entry<String, Pattern> entry : host.getPatterns().entrySet()) {
            Collection<LogSample> matched = service.loadLogs(resourceId, host.getLastReceivedLogDate(), FUTURE_DATE, Optional.of(entry.getValue()));
            if (!matched.isEmpty()) {
                notifications.add(new NotificationData(host.getHostName(), entry.getKey(), matched));
            }
        }

        return notifications;
    }

    private List<NotificationData> analyzeSequences() {

        List<NotificationData> notifications = new ArrayList<>();
        Date lastMatchDate = host.getLastReceivedLogDate();
        StringBuilder builder;
        for (Sequence seq : host.getSequences()) {
            boolean match = true;
            builder = new StringBuilder();
            for (Pattern pat : seq.getPatterns()) {
                Collection<LogSample> matched = service.loadLogs(resourceId, lastMatchDate, FUTURE_DATE, Optional.of(pat));
                if (matched.isEmpty()) {
                    match = false;
                    break;
                } else {
                    // optional must have value
                    lastMatchDate = matched.stream().map(LogSample::getTime).min(Date::compareTo).get();
                    builder.append(matched.iterator().next().getMessage()).append("\n");
                }
            }

            if (match) {
                notifications.add(new NotificationData(host.getHostName(), seq.getName(), Arrays.asList(new LogSample(lastMatchDate, builder.toString()))));
            }
        }

        return notifications;
    }

    private List<NotificationData> analyzeTimeSequences() {

        List<NotificationData> notifications = new ArrayList<>();
        List<Date> occurenceDates = new ArrayList<>();
        Date lastMatchDate = host.getLastReceivedLogDate();
        for (TimeSequence seq : host.getTimeSequences()) {
            boolean match = true;
            for (Pattern pat : seq.getPatterns()) {
                Collection<LogSample> matched = service.loadLogs(resourceId, lastMatchDate, FUTURE_DATE, Optional.of(pat));
                if (matched.isEmpty()) {
                    match = false;
                    break;
                } else {
                    // optional must have value
                    occurenceDates.add(lastMatchDate);
                    lastMatchDate = matched.stream().map(LogSample::getTime).min(Date::compareTo).get();
                }
            }

            if (match) {
                Date minDate = occurenceDates.stream().min(Date::compareTo).get();
                Date maxDate = occurenceDates.stream().max(Date::compareTo).get();
                long diffMinutes = ChronoUnit.MINUTES.between(minDate.toInstant(), maxDate.toInstant());
                if (diffMinutes <= seq.getInterval()) {
                    notifications.add(new NotificationData(host.getHostName(), seq.getName(), Collections.EMPTY_LIST));
                }
            }
        }

        return notifications;
    }
}
