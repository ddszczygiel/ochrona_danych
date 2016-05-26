package pl.agh.ochd.infrastructure;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.agh.ochd.model.LogSample;
import pl.agh.ochd.model.ResourceId;

import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

public class ElasticsearchPersistenceServiceTest {

    private static ElasticsearchPersistenceService service;

    @BeforeClass
    public static void setUp() throws UnknownHostException {

        service = new ElasticsearchPersistenceService("192.168.99.100", 9300);
    }

    @Test
    public void readWriteTest() {

        //given
        ResourceId id = new ResourceId("test_machine");
        Date now = new Date();
        Date nowPlus = Date.from(Instant.now().plusSeconds(60));
        Date nowMinus = Date.from(Instant.now().minusSeconds(60));
        Date future = Date.from(Instant.now().plusSeconds(600));
        LogSample s1 = new LogSample(now, "some message");
        LogSample s2 = new LogSample(nowPlus, "SOme message2");
        Pattern pattern = Pattern.compile("^SO.*");
        //when
        service.saveLogs(id, Arrays.asList(s1, s1));
        //then
        Collection<LogSample> logSamples = service.loadLogs(id, now, future, Optional.empty());
        Collection<LogSample> logSamplesPattern = service.loadLogs(id, now, future, Optional.of(pattern));
        Assert.assertEquals(2, logSamples.size());
        Assert.assertEquals(1, logSamplesPattern.size());
    }
}