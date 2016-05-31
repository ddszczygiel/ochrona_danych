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
        Date nowMinus = Date.from(Instant.now().minusSeconds(30));
        Date future = Date.from(Instant.now().plusSeconds(600));
        LogSample s1 = new LogSample(new Date(), "some test");
        LogSample s2 = new LogSample(new Date(), "some message included");
        Pattern pattern = Pattern.compile(".*message.*");
        //when
        service.saveLogs(id, Arrays.asList(s1, s2));
        //then
        Collection<LogSample> logSamples = service.loadLogs(id, nowMinus, future, Optional.empty());
        Collection<LogSample> logSamplesPattern = service.loadLogs(id, nowMinus, future, Optional.of(pattern));
        Assert.assertEquals(2, logSamples.size());
        Assert.assertEquals(1, logSamplesPattern.size());
    }
}