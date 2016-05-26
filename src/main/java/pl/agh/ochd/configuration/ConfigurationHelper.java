package pl.agh.ochd.configuration;


import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ConfigurationHelper {

    private ConfigurationHelper() {}

    public static Map<String, String> configToStringMap(Config config) {

        Map<String, String> map = new HashMap<>();
        config.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue().unwrapped().toString()));

        return map;
    }
}
