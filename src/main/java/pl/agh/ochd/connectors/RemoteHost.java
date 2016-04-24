package pl.agh.ochd.connectors;


import com.typesafe.config.Config;

import java.util.Date;
import java.util.Map;

public class RemoteHost {

    private ConnectorType connectorType;
    private String hostName;
    private String userName;
    private String passwd;
    private String logPath;
    private String logFile;
    private String logDateFormat;
    private String logDatePattern;
    private Date lastReceivedLogDate;
    private Map<String, String> properties;

    public RemoteHost(Config params) {

        // TODO
    }

    public ConnectorType getConnectorType() {
        return connectorType;
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

    public Date getLastReceivedLogDate() {
        return lastReceivedLogDate;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
