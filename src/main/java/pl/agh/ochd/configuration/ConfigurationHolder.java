package pl.agh.ochd.configuration;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import pl.agh.ochd.model.RemoteHost;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static pl.agh.ochd.configuration.ConfigurationHelper.configToStringMap;

public class ConfigurationHolder {

    private long refreshTime;
    private long maxOperationTime;

    private String emailAddress;
    private String emailLogin;
    private String emailPassword;
    private String emailHost;
    private String emailPort;

    private String dbAddress;
    private int dbPort;

    private List<RemoteHost> hosts;
    private Map<String, String> emails;
    private Map<String, Pattern> patterns;

    public ConfigurationHolder(Config configuration) {

        this.refreshTime = configuration.getLong("refresh.time");
        this.maxOperationTime = configuration.getLong("max.operation.time");

        this.emailAddress = configuration.getString("configuration.email.address");
        this.emailLogin = configuration.getString("configuration.email.login");
        this.emailPassword = configuration.getString("configuration.email.password");
        this.emailHost = configuration.getString("email.smtp.host");
        this.emailPort = configuration.getString("email.smtp.port");

        this.dbAddress = configuration.getString("db.address");
        this.dbPort = configuration.getInt("db.port");

        this.hosts = prepareHosts(configuration.getConfigList("configuration.remote.hosts"));
        this.emails = configToStringMap(configuration.getConfig("configuration.emails"));
        this.patterns = getPatterns(configuration.getConfig("global.patterns"));
    }


    private List<RemoteHost> prepareHosts(List<? extends Config> hosts) {

        return hosts.stream().map(RemoteHost::new).collect(Collectors.toList());
    }

    private Map<String, Pattern> getPatterns(Config config) {

        Map<String, String> stringMap = configToStringMap(config);
        Map<String, Pattern> patternMap = new HashMap<>();
        stringMap.forEach((key, val) -> patternMap.put(key, Pattern.compile(val)));
        return patternMap;
    }

    public long getRefreshTime() {
        return refreshTime;
    }

    public long getMaxOperationTime() {
        return maxOperationTime;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEmailLogin() {
        return emailLogin;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public String getEmailPort() {
        return emailPort;
    }

    public String getDbAddress() {
        return dbAddress;
    }

    public int getDbPort() {
        return dbPort;
    }

    public List<RemoteHost> getHosts() {
        return hosts;
    }

    public Map<String, String> getEmails() {
        return emails;
    }

    public Map<String, Pattern> getPatterns() {
        return patterns;
    }
}
