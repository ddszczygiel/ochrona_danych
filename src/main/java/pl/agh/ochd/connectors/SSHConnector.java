package pl.agh.ochd.connectors;


import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import pl.agh.ochd.model.RemoteHost;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SSHConnector implements Connector {

    private static final String SSH_COMMAND = "cat %s%s";

    private RemoteHost host;
    private Shell shell;

    public SSHConnector(RemoteHost host) {

        this.host = host;
    }

    private boolean connect() {

        try {
            shell = new SSH(host.getHostName(), 22, host.getUserName(), host.getPasswd());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<List<String>> getLogs() {

        boolean isConnected = connect();
        if (!isConnected) {
            return Optional.empty();
        }

        String command = String.format(SSH_COMMAND, host.getLogPath(), host.getLogFile());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int result = shell.exec(command, null, out, null);
            if (result != 0) {
                return Optional.empty();
            }
            String logs = out.toString(StandardCharsets.UTF_8.name());
            return Optional.of(new ArrayList<>(Arrays.asList(logs.split("\\n"))));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
