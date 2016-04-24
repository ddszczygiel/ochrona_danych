package pl.agh.ochd;

import com.google.common.collect.Lists;
import pl.agh.ochd.domain.LogSample;
import pl.agh.ochd.domain.ResourceId;
import pl.agh.ochd.infrastructure.ElasticsearchPersistenceService;
import pl.agh.ochd.persistence.PersistenceService;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author mciolek
 */
public class Main {

    private static Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static void main(String args[]) throws UnknownHostException {
        PersistenceService service = new ElasticsearchPersistenceService("192.168.99.100", 9300);
        LocalDateTime now = LocalDateTime.now();
        LogSample sample1 = new LogSample(toDate(now), "Kot");
        LogSample sample2 = new LogSample(toDate(now.plusSeconds(10)), "Tomek");

        service.saveLogs(new ResourceId("test"), Lists.newArrayList(sample1, sample2));

        Pattern pattern = Pattern.compile("ala.*");

        Collection<LogSample> result = service.loadLogs(new ResourceId("test"), toDate(now.minusDays(1)), toDate(now.plusDays(1)), Optional.of(pattern));
        result.forEach(log -> System.out.println(String.format("%s -> %s", log.getTime(), log.getMessage())));

    }
}
