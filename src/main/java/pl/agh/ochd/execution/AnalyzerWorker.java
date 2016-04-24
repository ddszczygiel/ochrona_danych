package pl.agh.ochd.execution;


import pl.agh.ochd.alerting.AlertingService;
import pl.agh.ochd.connectors.Connector;
import pl.agh.ochd.connectors.ConnectorFactory;
import pl.agh.ochd.connectors.RemoteHost;
import pl.agh.ochd.domain.LogSample;
import pl.agh.ochd.domain.ResourceId;
import pl.agh.ochd.logs.LogHelper;
import pl.agh.ochd.infrastructure.PersistenceService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class AnalyzerWorker implements Callable<Boolean> {

    private static final Date FUTURE_DATE = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

    private int callCount;
    private RemoteHost host;
    private Connector connector;
    private PersistenceService service;
    private AlertingService alertingService;
    private LogHelper helper;
    private ResourceId resourceId;
    private Map<String, Pattern> patterns;


    public AnalyzerWorker(RemoteHost host, PersistenceService service, AlertingService alertingService, Map<String, Pattern> patterns) {

        this.connector = ConnectorFactory.getConnector(host);
        this.host = host;
        this.service = service;
        this.alertingService = alertingService;
        this.patterns = patterns;
        this.resourceId = new ResourceId(host.getHostName());
        this.helper = new LogHelper();
    }

    @Override
    public Boolean call() throws Exception {

        callCount++;
        // get logs
        Optional<List<String>> logs = connector.getLogs();
        if (!logs.isPresent()) {
            // something goes wrong
            return false;
        }
        List<String> lines = logs.get();
        // prepare to insert
        List<LogSample> logSamples = helper.prepareLogsToInsert(lines, host.getLastReceivedLogDate(), host.getLogDateFormat(), host.getLogDatePattern());
        if (logSamples.size() == 0) {
            // no new logs
            return true;
        }
        // insert to db
        service.saveLogs(resourceId, logSamples);
        // check for errors
        patterns.forEach((key, val) -> {
            Collection<LogSample> matched = service.loadLogs(resourceId, host.getLastReceivedLogDate(), FUTURE_DATE, Optional.of(val));
            // send alert mails
            if (!matched.isEmpty()) {
                alertingService.sendAlertNotification(host.getHostName(), matched, key);
            }
        });

        // TODO replace lastRetrieved date SOMEWHERE
        return true;
    }

    public int getCallCount() {
        return callCount;
    }

    public void resetCallCount() {
        callCount = 0;
    }
}
