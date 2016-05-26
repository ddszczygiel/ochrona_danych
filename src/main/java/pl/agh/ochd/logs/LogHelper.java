package pl.agh.ochd.logs;


import pl.agh.ochd.model.LogSample;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogHelper {

    private static final String TIMESTAMP_GROUP = "(?<timestamp>%s)";

    public List<LogSample> convertToDomainModel(List<String> logs, String logDateFormat, String logDatePattern) {

        String namedTimestampGroup = String.format(TIMESTAMP_GROUP, logDatePattern);
        Pattern timestampPattern = Pattern.compile(namedTimestampGroup);
        SimpleDateFormat formatter = new SimpleDateFormat(logDateFormat);

        List<LogSample> list = new ArrayList<>();
        for (String line : logs) {
            Matcher match = timestampPattern.matcher(line);
            if (match.find()) {
                String timestamp = match.group("timestamp");
                Date timestampDate;
                try {
                    timestampDate = formatter.parse(timestamp);
                    list.add(new LogSample(timestampDate, line));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return list;
    }

    public Date getLastReceivedLogDate(String lastLog, String logDateFormat, String logDatePattern) {

        String namedTimestampGroup = String.format(TIMESTAMP_GROUP, logDatePattern);
        Pattern timestampPattern = Pattern.compile(namedTimestampGroup);
        SimpleDateFormat formatter = new SimpleDateFormat(logDateFormat);

        Matcher match = timestampPattern.matcher(lastLog);
        if (match.find()) {
            String timestamp = match.group("timestamp");
            try {
                return formatter.parse(timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;
    }
}
