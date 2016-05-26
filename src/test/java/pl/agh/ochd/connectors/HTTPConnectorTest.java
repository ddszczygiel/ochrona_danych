package pl.agh.ochd.connectors;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.agh.ochd.model.RemoteHost;

import java.util.List;
import java.util.Optional;

public class HTTPConnectorTest {

    private HTTPConnector http;
    private RemoteHost testHost;

    @Before
    public void setUp() {

        testHost = new RemoteHost(null, "dominik", "dominik4321", 0, "application.log", "http://91.240.28.246:20090/");
        http = new HTTPConnector(testHost);
        // whole remote log has content length of: 81899 bytes
    }

    @Test
    public void testConnection() {

        //given
        String url = testHost.getLogPath() + testHost.getLogFile();
        //when
        Optional<List<String>> lines = http.getLines(url, Optional.empty());
        //then
        Assert.assertTrue(lines.isPresent());
        Assert.assertEquals(562, lines.get().size());
    }

    @Test
    public void testPartialContent() {

        // whole remote log has content length of: 81899 bytes so trimming it form 40000 should give content length = 41899
        // that we see on app logs

        //given
        String url = testHost.getLogPath() + testHost.getLogFile();
        //when
        Optional<List<String>> lines = http.getLines(url, Optional.of(40000L));
        //then
        Assert.assertTrue(lines.isPresent());
        Assert.assertEquals(41899L, testHost.getLastReceivedByte());
    }

}