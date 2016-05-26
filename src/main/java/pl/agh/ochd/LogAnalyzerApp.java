package pl.agh.ochd;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.agh.ochd.alerting.AlertingService;
import pl.agh.ochd.configuration.ConfigurationHolder;
import pl.agh.ochd.connectors.ConnectorType;
import pl.agh.ochd.execution.AnalyzerWorker;
import pl.agh.ochd.infrastructure.ElasticsearchPersistenceService;
import pl.agh.ochd.infrastructure.PersistenceService;
import pl.agh.ochd.model.NotificationData;
import pl.agh.ochd.model.RemoteHost;

import java.net.URL;
import java.net.UnknownHostException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class LogAnalyzerApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogAnalyzerApp.class);

    private Config config;
    private ConfigurationHolder configurationHolder;
    private PersistenceService persistenceService;
    private AlertingService alertingService;

    private List<AnalyzerWorker> workers;
    private ExecutorService executors;


    public void loadConfiguration() {

        config = ConfigFactory.load();
        configurationHolder = new ConfigurationHolder(config);
    }

    public void init() throws UnknownHostException {

        LOGGER.debug("Initializing App components");
        persistenceService = new ElasticsearchPersistenceService(configurationHolder.getDbAddress(),
                                                                configurationHolder.getDbPort());

        alertingService = new AlertingService(configurationHolder.getEmailAddress(), configurationHolder.getEmailLogin(),
                                              configurationHolder.getEmailPassword(), configurationHolder.getEmailHost(),
                                                configurationHolder.getEmailPort(), configurationHolder.getEmails().values());

        executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        workers = configurationHolder.getHosts().stream()
                .map(host -> new AnalyzerWorker(host, persistenceService))
                .collect(Collectors.toList());
    }

    private void prepareWorkers() {

//        URL url = Main.class.getClassLoader().getResource("logexample.log");
//        String path = url.getPath();
//
////        mmdd hh:mm:ss.uuuuuu
//        RemoteHost mocked = new RemoteHost(ConnectorType.MOCK, "machine1","MMddyyyy hh:mm:ss", "\\d{8} \\d{2}:\\d{2}:\\d{2}");
//        workers.add(new AnalyzerWorker(mocked, persistenceService, alertingService, configurationHolder.getPatterns()));


    }

    public void start() {

        LOGGER.debug("Starting application...");
        List<NotificationData> notifications;
        while (true) {

            long t1 = System.currentTimeMillis();
            notifications = new ArrayList<>();
            try {
                List<Future<Collection<NotificationData>>> results = executors.invokeAll(workers);
                for (Future<Collection<NotificationData>> result : results) {
                    try {
                        notifications.addAll(result.get(configurationHolder.getMaxOperationTime(), TimeUnit.SECONDS));
                    } catch (ExecutionException e) {
                        LOGGER.error("Exception during computation", e);
                    } catch (TimeoutException e) {
                        LOGGER.error("Timeout occurred", e);
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.error("Could not invoke worker", e);
            }
            long t2 = System.currentTimeMillis();

            LOGGER.debug("Computation time: " + TimeUnit.MILLISECONDS.toSeconds(t2-t1) + "[s]");
            LOGGER.debug("Alerts count: " + notifications.size());
            notifications.forEach(alertingService::sendAlertNotification);
            try {
                TimeUnit.SECONDS.sleep(configurationHolder.getRefreshTime());
            } catch (InterruptedException e) {
                LOGGER.error("Thread interrupted", e);
            }
        }
    }

    public Config getConfig() {
        return config;
    }

    public ConfigurationHolder getConfigurationHolder() {
        return configurationHolder;
    }

    public static void main(String[] args) throws UnknownHostException {

        LogAnalyzerApp app = new LogAnalyzerApp();
        app.loadConfiguration();
        app.init();
        app.start();
    }
}
