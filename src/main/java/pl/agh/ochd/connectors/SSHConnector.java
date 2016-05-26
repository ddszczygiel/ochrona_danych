package pl.agh.ochd.connectors;


import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.agh.ochd.model.RemoteHost;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SSHConnector implements Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHConnector.class);

    private static final String WHOLE_FILE = "tail %s";
    private static final String FROM_LINE = "line=`grep -n %s %s | cut -d ':' -f 1`; tail --line=+$((line+1)) %s";
    private static final String ROLLED_LINES =
            "line=`grep -n %s %s | cut -d ':' -f 1`; tail --line=+$((line+1)) %s > XD.log; tail %s >> XD.log; tail XD.log; rm XD.log";

    private JSch jsch;
    private Session session;
    private RemoteHost host;

    public SSHConnector(RemoteHost host) {

        this.host = host;
        this.jsch = new JSch();
    }

    boolean connect() {

        try {
            session = jsch.getSession(host.getUserName(), host.getHostName(), host.getPort());
            session.setPassword(host.getPasswd());
            session.setConfig("StrictHostKeyChecking", "no");
            LOGGER.debug("Successfully opened SSH session");
            session.connect();
            return true;
        } catch (JSchException e) {
            LOGGER.error("Could not open SSH session", e);
            return false;
        }
    }

    void disconnect() {

        if (session != null && session.isConnected()) {
            LOGGER.debug("Closing SSH session");
            session.disconnect();
        }
    }

    Optional<String> executeCommand(String command) {

        if (!session.isConnected()) {
            return Optional.empty();
        }

        Channel channel = null;
        try {
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            InputStream commandResult = channel.getInputStream();
            LOGGER.debug("Executing command: " + command);
            channel.connect();
            return Optional.of(IOUtils.toString(commandResult, Charset.forName(StandardCharsets.UTF_8.name())).trim());
        } catch (JSchException e) {
            LOGGER.error("Error occurred during SSH command execution", e);
        } catch (IOException e) {
            LOGGER.error("Error occurred during SSH input stream operation", e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<String>> getLogs() {

        if (!connect()) {
            return Optional.empty();
        }

        String command = isFileRolled(host) ? getRolledLinesCommand() : getLinesCommand();
        Optional<String> result = executeCommand(command);
        disconnect();

        if (result.isPresent()) {
            return Optional.of(new ArrayList<>(Arrays.asList(result.get().split("\\n"))));
        } else {
            return Optional.empty();
        }
    }

    private String getLinesCommand() {

        String logFileFullPath = host.getLogPath() + host.getLogFile();
        if (host.getLastReceivedLogDate() == null) {
            return String.format(WHOLE_FILE, logFileFullPath);
        } else {
            SimpleDateFormat logDateFormatter = new SimpleDateFormat(host.getLogDatePattern());
            String lastLineText = logDateFormatter.format(host.getLastReceivedLogDate());
            return String.format(FROM_LINE, lastLineText, logFileFullPath, logFileFullPath);
        }
    }

    private String getRolledLinesCommand() {

        Date yesterday = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
        SimpleDateFormat rolledFormatter = new SimpleDateFormat(host.getOldLogPattern());

        String rolledFileFullPath = host.getLogPath() + rolledFormatter.format(yesterday);
        String logFileFullPath = host.getLogPath() + host.getLogFile();

        SimpleDateFormat logDateFormatter = new SimpleDateFormat(host.getLogDatePattern());
        String lastLineText = logDateFormatter.format(host.getLastReceivedLogDate());

        return String.format(ROLLED_LINES, lastLineText, rolledFileFullPath, rolledFileFullPath, logFileFullPath);
    }

}