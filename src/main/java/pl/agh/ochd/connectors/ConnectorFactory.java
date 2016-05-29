
package pl.agh.ochd.connectors;

import pl.agh.ochd.model.RemoteHost;

import java.net.URL;

public final class ConnectorFactory {

    private static URL url = ConnectorFactory.class.getClassLoader().getResource("/logs.log");
    private static String path = url.getPath();

    private ConnectorFactory() {}

    // move it to Connector and make it as abstract ??
    public static Connector getConnector(RemoteHost remoteHost) {

        switch (remoteHost.getConnectorType()) {

            case SSH:
                return new SSHConnector(remoteHost);
            case FTP:
                return new FTPConnector(remoteHost);
            case HTTP:
                return new HTTPConnector(remoteHost);
            case MOCK:
                return new MockConnector(path);
            default:
                throw new IllegalStateException("Invalid requested connector type");
        }
    }
}
