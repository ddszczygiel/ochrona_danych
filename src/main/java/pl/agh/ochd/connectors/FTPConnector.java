package pl.agh.ochd.connectors;


import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import pl.agh.ochd.model.RemoteHost;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FTPConnector implements Connector {

    private RemoteHost host;
    private FTPClient ftpClient;

    public FTPConnector(RemoteHost host) {

        this.host = host;
        ftpClient = new FTPClient();
    }

    private boolean connect() {

        try {
            ftpClient.connect(host.getLogPath(), 21);
            if (host.getUserName() != null && host.getPasswd() != null) {
                ftpClient.login(host.getUserName(), host.getPasswd());
            }
            ftpClient.enterLocalPassiveMode(); // ???
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE); // ???
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            disconnect();
        }
    }

    private void disconnect() {

        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Optional<List<String>> getLogs() {

        boolean isConnected = connect();
        if (!isConnected) {
            return Optional.empty();
        }

        try {
            // FIXME figure out how to set this header with range
            InputStream fileStream = ftpClient.retrieveFileStream(host.getLogPath()+host.getLogFile());
            String logs = IOUtils.toString(fileStream);
            ftpClient.completePendingCommand();
            ftpClient.disconnect();
            return Optional.of(new ArrayList<>(Arrays.asList(logs.split("\\n"))));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            disconnect();
        }
    }
}
