package pl.agh.ochd.logs;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.agh.ochd.model.LogSample;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LogHelperTest {

    private static LogHelper logHelper;

    @BeforeClass
    public static void setUp() {

        logHelper = new LogHelper();
    }

    @Test
    public void lastReceivedDatePattern() {

        //given
        String lastLog = "I01112015 22:04:48.733120 25832 slave.cpp:3926] Current disk usage 3.83%. Max allowed age: 6.031894171438750days\n";
        String logDateFormat = "MMddyyyy hh:mm:ss";
        String logDatePattern = "\\d{8} \\d{2}:\\d{2}:\\d{2}";
        //from Date class int y = year + 1900;
        Date expected = new Date(115, 0, 11, 22, 4, 48);
        //when
        Date lastReceivedLogDate = logHelper.getLastReceivedLogDate(lastLog, logDateFormat, logDatePattern);
        //then
        Assert.assertEquals(expected, lastReceivedLogDate);
    }

    @Test
    public void conversionTest() {

        //given
        String log = "I01112015 22:04:48.733120 25832 slave.cpp:3926] Current disk usage 3.83%. Max allowed age: 6.031894171438750days\n";
        String logDateFormat = "MMddyyyy hh:mm:ss";
        String logDatePattern = "\\d{8} \\d{2}:\\d{2}:\\d{2}";
        Date expected = new Date(115, 0, 11, 22, 4, 48);
        List<String> lines = new ArrayList<>();
        lines.add(log);
        //when
        List<LogSample> logSamples = logHelper.convertToDomainModel(lines, logDateFormat, logDatePattern);
        //then
        Assert.assertEquals(1, logSamples.size());
        Assert.assertEquals(log, logSamples.get(0).getMessage());
        Assert.assertEquals(expected, logSamples.get(0).getTime());
    }

}