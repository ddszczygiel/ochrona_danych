package pl.agh.ochd.connectors;


import pl.agh.ochd.model.RemoteHost;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface Connector {

    SimpleDateFormat COMPARE_FORMAT = new SimpleDateFormat("ddMM");

    Optional<List<String>> getLogs();

    default boolean isFileRolled(RemoteHost host) {

        if (host.getLastReceivedLogDate() == null) {
            return false;
        }
        return !COMPARE_FORMAT.format(new Date()).equals(COMPARE_FORMAT.format(host.getLastReceivedLogDate()));
    }
}
