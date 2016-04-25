package pl.agh.ochd.model;


import com.typesafe.config.Config;
import pl.agh.ochd.connectors.ConnectorType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

        this.connectorType = ConnectorType.valueOf(params.getString("connectorType"));
        this.hostName = params.getString("hostName");
        this.userName = params.getString("userName");
        this.passwd = params.getString("passwd");
        this.logPath = params.getString("logPath");
        this.logFile = params.getString("logFile");
        this.logDateFormat = params.getString("logDateFormat");
        this.logDatePattern = params.getString("logDatePattern");
        this.lastReceivedLogDate = Date.from(Instant.now().minus(6, ChronoUnit.MONTHS));  // TODO
        this.properties = null; // TODO
    }

    // for tests purpose
    public RemoteHost(ConnectorType connectorType, String hostName, String logDateFormat, String logDatePattern) {

        this.connectorType = connectorType;
        this.hostName = hostName;
        this.logDateFormat = logDateFormat;
        this.logDatePattern = logDatePattern;
        this.lastReceivedLogDate = Date.from(Instant.now().minus(100, ChronoUnit.DAYS));  // TODO
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

    public void setLastReceivedLogDate(Date lastReceivedLogDate) {
        this.lastReceivedLogDate = lastReceivedLogDate;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
