package pl.agh.ochd;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import pl.agh.ochd.connectors.RemoteHost;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigurationHolder {

    private Map<String, String> emails;
    private Map<String, Pattern> patterns;
    private List<RemoteHost> hosts;
    private String emailHost;
    private String emailAddress;


    public ConfigurationHolder(Config configuration) {

        this.emails = configToStringMap(configuration.getConfig("configuration.emails"));
        this.patterns = preparePatternMap(configuration.getConfig("configuration.patterns"));
        this.hosts = prepareHosts(configuration.getConfigList("configuration.remote.hosts"));
        this.emailHost = configuration.getString("configuration.email.host");
        this.emailAddress = configuration.getString("configuration.email.address");

    }

    private Map<String, String> configToStringMap(Config config) {

        Map<String, String> map = new HashMap<>();
        Set<Map.Entry<String, ConfigValue>> values = config.entrySet();
        values.forEach(entry -> map.put(entry.getKey(), entry.getValue().unwrapped().toString()));

        return map;
    }

    private Map<String, Pattern> preparePatternMap(Config config) {

        Map<String, String> stringMap = configToStringMap(config);
        Map<String, Pattern> patternMap = new HashMap<>();
        stringMap.forEach((key, val) -> patternMap.put(key, Pattern.compile(val)));

        return patternMap;
    }

    private List<RemoteHost> prepareHosts(List<? extends Config> hosts) {

        return hosts.stream().map(RemoteHost::new).collect(Collectors.toList());
    }

    public Map<String, String> getEmails() {
        return emails;
    }

    public Map<String, Pattern> getPatterns() {
        return patterns;
    }

    public List<RemoteHost> getHosts() {
        return hosts;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}
