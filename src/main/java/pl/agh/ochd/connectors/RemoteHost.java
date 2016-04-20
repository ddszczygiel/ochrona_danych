package pl.agh.ochd.connectors;


import java.util.Map;

public class RemoteHost {

    private ConnectorType connectorType;
    private String hostName;
    private String userName;
    private String passwd;
    private String logPath;
    private String logFile;
    private String logDateFormat;
    private Map<String, String> properties;

    public RemoteHost(String hostName, String userName, String passwd, String logPath, String logFile,
                      String logDateFormat, ConnectorType connectorType ,Map<String, String> properties) {

        this.connectorType = connectorType;
        this.hostName = hostName;
        this.userName = userName;
        this.passwd = passwd;
        this.logPath = logPath;
        this.logFile = logFile;
        this.logDateFormat = logDateFormat;
        this.properties = properties;
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

    public ConnectorType getConnectorType() {
        return connectorType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
