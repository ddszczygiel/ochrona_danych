package pl.agh.ochd.connectors;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pl.agh.ochd.model.RemoteHost;

import java.util.Optional;

public class SSHConnectorTest {

    private SSHConnector ssh;
    private RemoteHost testHost;

    @Before
    public void setUp() {

        testHost = new RemoteHost("91.240.28.246", "dominik", "dominik4321", 20080, null, null);
        ssh = new SSHConnector(testHost);
    }

    @Test
    public void testSSHConnection() {

        //given
        //when
        ssh.connect();
        Optional<String> result = ssh.executeCommand("pwd");
        ssh.disconnect();
        //then
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals("/home/dominik", result.get());
    }
}