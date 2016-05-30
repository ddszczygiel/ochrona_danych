package pl.agh.ochd;

import pl.agh.ochd.connectors.ConnectorType;
import pl.agh.ochd.infrastructure.ElasticsearchPersistenceService;
import pl.agh.ochd.infrastructure.PersistenceService;
import pl.agh.ochd.model.LogSample;
import pl.agh.ochd.model.RemoteHost;
import pl.agh.ochd.model.ResourceId;

import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;


public class Main {

    private static URL url = Main.class.getClassLoader().getResource("logs.log");
    private static String path = url.getPath();

    private static Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static void main(String args[]) throws UnknownHostException, InterruptedException {

//        MockConnector mockConnector = new MockConnector(path);
//        for (int i = 0; i < 3; i++) {
//            List<String> lines = mockConnector.getLogs().get();
//            System.out.println(lines);
//            System.out.println(lines.size());
//            System.out.println("\n\n");
//        }
        PersistenceService service = new ElasticsearchPersistenceService("192.168.99.100", 9300);
        Date lastReceived = Date.from(Instant.now().minusSeconds(157784630));
        Date future = Date.from(Instant.now().plusSeconds(100000));
//        PersistenceService service = new ElasticsearchPersistenceService("192.168.99.100", 2376);
//        service.saveLogs(new ResourceId("dupa"), Arrays.asList(new LogSample(new Date(), "ABcadlo")));
//        service.saveLogs(new ResourceId("dupa"), Arrays.asList(new LogSample(new Date(), "abecadlo")));
//        service.saveLogs(new ResourceId("dupa"), Arrays.asList(new LogSample(new Date(), "nanananno")));
//        LocalDateTime now = LocalDateTime.now();
//
//        Pattern pattern = Pattern.compile(".*AB.*");
//
        Collection<LogSample> result = service.loadLogs(new ResourceId("machine101"), lastReceived, future, Optional.empty());
//        Collection<LogSample> result = service.loadLogs(new ResourceId("dupa"), toDate(now.minusMinutes(10)), toDate(now), Optional.of(pattern));
        result.forEach(log -> System.out.println(String.format("%s -> %s", log.getTime(), log.getMessage())));
//        RemoteHost mockedHost = new RemoteHost(ConnectorType.MOCK, "machine101","MMddyyyy hh:mm:ss", "\\d{8} \\d{2}:\\d{2}:\\d{2}", lastReceived, patterns, sequences);

    }
}
