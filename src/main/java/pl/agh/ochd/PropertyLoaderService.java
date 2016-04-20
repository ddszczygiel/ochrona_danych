package pl.agh.ochd;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyLoaderService {

    private static final String PATTERNS_FILE = "/patterns.prop";
    private static final String EMAILS_FILE = "/emails.prop";

    private Map<String, String> emails;
    private Map<String, String> patterns;

    public PropertyLoaderService() throws IOException {

        emails = propertiesToMap(loadProperties(EMAILS_FILE));
        patterns = propertiesToMap(loadProperties(PATTERNS_FILE));
    }

    public Map<String, String> getEmails() {
        return emails;
    }

    public Map<String, String> getPatterns() {
        return patterns;
    }

    private Properties loadProperties(String fileName) throws IOException {

        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        Properties prop = new Properties();
        prop.load(inputStream);
        inputStream.close();

        return prop;
    }

    private Map<String, String> propertiesToMap(Properties properties) {

        Map<String, String> map = new HashMap<String, String>();
        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }

        return map;
    }

}
