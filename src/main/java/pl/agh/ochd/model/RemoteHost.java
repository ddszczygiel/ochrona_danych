package pl.agh.ochd.model;


import com.typesafe.config.Config;
import pl.agh.ochd.connectors.ConnectorType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static pl.agh.ochd.configuration.ConfigurationHelper.configToStringMap;

public class RemoteHost {

    private ConnectorType connectorType;
    private int port;
    private String hostName;
    private String userName;
    private String passwd;
    private String logPath;
    private String logFile;
    private String logDateFormat;
    private String logDatePattern;
    private String oldLogPattern;
    private Date lastReceivedLogDate;
    private long lastReceivedByte;
    private Map<String, Pattern> patterns;
    private Map<String, String> properties;
    private List<Sequence> sequences;
    private List<TimeSequence> timeSequences;

    public RemoteHost(Config params) {

        this.connectorType = ConnectorType.valueOf(params.getString("connectorType"));
        this.port = params.getInt("port");
        this.hostName = params.getString("hostName");
        this.userName = params.getString("userName");
        this.passwd = params.getString("passwd");
        this.logPath = params.getString("logPath");
        this.logFile = params.getString("logFile");
        this.logDateFormat = params.getString("logDateFormat");
        this.logDatePattern = params.getString("logDatePattern");
        this.oldLogPattern = params.getString("oldLogPattern");
        this.lastReceivedLogDate = null;
        this.lastReceivedByte = params.getLong("lastReceivedByte");
        this.properties = configToStringMap(params.getConfig("properties"));
        this.patterns = preparePatternMap(params.getConfig("patterns"));
        this.sequences = params.getConfigList("sequences").stream().map(Sequence::new).collect(Collectors.toList());
        this.timeSequences = params.getConfigList("sequences").stream().map(TimeSequence::new).collect(Collectors.toList());
    }

    // for tests purpose
    public RemoteHost(ConnectorType connectorType, String hostName, String logDateFormat, String logDatePattern) {

        this.connectorType = connectorType;
        this.hostName = hostName;
        this.logDateFormat = logDateFormat;
        this.logDatePattern = logDatePattern;
        this.lastReceivedLogDate = Date.from(Instant.now().minus(100, ChronoUnit.DAYS));  // TODO
    }

    public RemoteHost(String hostName, String userName, String passwd, int port) {

        this.hostName = hostName;
        this.userName = userName;
        this.passwd = passwd;
        this.port = port;
    }

    private Map<String, Pattern> preparePatternMap(Config config) {

        Map<String, String> stringMap = configToStringMap(config);
        Map<String, Pattern> patternMap = new HashMap<>();
        stringMap.forEach((key, val) -> patternMap.put(key, Pattern.compile(val)));

        return patternMap;
    }

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public int getPort() {
        return port;
    }

    public String getHostName() {
        return hostName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPasswd() {
        return passwd;
    }

    public String getLogPath() {
        return logPath;
    }

    public String getLogFile() {
        return logFile;
    }

    public String getLogDateFormat() {
        return logDateFormat;
    }

    public String getLogDatePattern() {
        return logDatePattern;
    }

    public String getOldLogPattern() {
        return oldLogPattern;
    }

    public Date getLastReceivedLogDate() {
        return lastReceivedLogDate;
    }

    public long getLastReceivedByte() {
        return lastReceivedByte;
    }

    public Map<String, Pattern> getPatterns() {
        return patterns;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<Sequence> getSequences() {
        return sequences;
    }

    public List<TimeSequence> getTimeSequences() {
        return timeSequences;
    }

    public void setLastReceivedLogDate(Date lastReceivedLogDate) {
        this.lastReceivedLogDate = lastReceivedLogDate;
    }

    public void setLastReceivedByte(long lastReceivedByte) {
        this.lastReceivedByte = lastReceivedByte;
    }
}
