package pl.agh.ochd;

import pl.agh.ochd.infrastructure.ElasticsearchPersistenceService;
import pl.agh.ochd.infrastructure.PersistenceService;
import pl.agh.ochd.model.LogSample;
import pl.agh.ochd.model.ResourceId;

import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;


public class Main {

    private static URL url = Main.class.getClassLoader().getResource("logexample.log");
    private static String path = url.getPath();

    private static Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static void main(String args[]) throws UnknownHostException, InterruptedException {

        PersistenceService service = new ElasticsearchPersistenceService("192.168.99.100", 9300);
//        service.saveLogs(new ResourceId("dupa"), Arrays.asList(new LogSample(new Date(), "trololo")));
        LocalDateTime now = LocalDateTime.now();

        Pattern pattern = Pattern.compile(".*Detecting new master.*");

        Collection<LogSample> result = service.loadLogs(new ResourceId("machine1"), toDate(now.minusYears(100)), toDate(now), Optional.empty());
        result.forEach(log -> System.out.println(String.format("%s -> %s", log.getTime(), log.getMessage())));
    }
}
