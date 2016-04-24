package pl.agh.ochd;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import pl.agh.ochd.alerting.AlertingService;
import pl.agh.ochd.infrastructure.ElasticsearchPersistenceService;
import pl.agh.ochd.infrastructure.PersistenceService;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LogAnalyzerApp {

    private Config config;
    private ConfigurationHolder configurationHolder;
    private PersistenceService persistenceService;
    private AlertingService alertingService;

    private ExecutorService executors;


    public void loadConfiguration() {

        config = ConfigFactory.load();
        configurationHolder = new ConfigurationHolder(config);
    }

    public void init() throws UnknownHostException {

        persistenceService = new ElasticsearchPersistenceService("192.168.99.100", 9300);
        alertingService = new AlertingService(configurationHolder.getEmailHost(), configurationHolder.getEmailAddress(),
                                              configurationHolder.getEmails().values());
        executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }



    public Config getConfig() {
        return config;
    }

    public ConfigurationHolder getConfigurationHolder() {
        return configurationHolder;
    }

    public static void main(String[] args) {

        LogAnalyzerApp app = new LogAnalyzerApp();
        app.loadConfiguration();
        System.out.println(app.getConfigurationHolder().getEmails());
    }
}
