package pl.agh.ochd;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import pl.agh.ochd.alerting.AlertingService;
import pl.agh.ochd.connectors.ConnectorType;
import pl.agh.ochd.connectors.MockConnector;
import pl.agh.ochd.execution.AnalyzerWorker;
import pl.agh.ochd.infrastructure.ElasticsearchPersistenceService;
import pl.agh.ochd.infrastructure.PersistenceService;
import pl.agh.ochd.model.RemoteHost;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class LogAnalyzerApp {

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

        persistenceService = new ElasticsearchPersistenceService("192.168.99.100", 9300);
        alertingService = new AlertingService(configurationHolder.getEmailAddress(), configurationHolder.getEmailLogin(),
                                              configurationHolder.getEmailPassword(), configurationHolder.getEmails().values());
        executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        workers = new ArrayList<>();
    }

    private void prepareWorkers() {

        URL url = Main.class.getClassLoader().getResource("logexample.log");
        String path = url.getPath();

//        mmdd hh:mm:ss.uuuuuu
        RemoteHost mocked = new RemoteHost(ConnectorType.MOCK, "machine1","MMddyyyy hh:mm:ss", "\\d{8} \\d{2}:\\d{2}:\\d{2}");
        workers.add(new AnalyzerWorker(mocked, persistenceService, alertingService, configurationHolder.getPatterns()));

        // FIXME uncomment
//        workers.addAll(configurationHolder.getHosts().stream()
//                .map(rh -> new AnalyzerWorker(rh, persistenceService, alertingService, configurationHolder.getPatterns()))
//                .collect(Collectors.toList()));
    }

    public void start() {

        prepareWorkers();
        while (true) {

            List<Future<Boolean>> holder = new ArrayList<>();
            workers.forEach(worker -> holder.add(executors.submit(worker)));

            // TODO fail handling and retrying
            for (Future<Boolean> future : holder) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            // TODO check condition and do retrying while possible
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {}
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
