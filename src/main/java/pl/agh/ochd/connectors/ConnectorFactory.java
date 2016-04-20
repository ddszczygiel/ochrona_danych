package pl.agh.ochd.connectors;


public final class ConnectorFactory {

    private ConnectorFactory() {}

    // move it to Connector and make it as abstract ??
    public static Connector getConnector(RemoteHost remoteHost) throws Exception {

        switch (remoteHost.getConnectorType()) {

            case SSH:
                return new SSHConnector(remoteHost);
            case FTP:
                return new FTPConnector(remoteHost);
            case HTTP:
                return new SSHConnector(remoteHost);
            default:
                throw new Exception("No such connector type !");
        }
    }
}
