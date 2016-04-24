package pl.agh.ochd.logs;


import pl.agh.ochd.domain.LogSample;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogHelper {

    private static final String TIMESTAMP_GROUP = "(?<timestamp>%s)";


    private List<String> filterLatestLogs(List<String> logs, Date lastReceivedLogDate, String logDateFormat, String logDatePattern) {

        int pos = 0;
        String namedTimestampGroup = String.format(TIMESTAMP_GROUP, logDatePattern);
        SimpleDateFormat formatter = new SimpleDateFormat(logDateFormat);
        String lastReceivedLogDateText = formatter.format(lastReceivedLogDate);
        Pattern timestampPattern = Pattern.compile(namedTimestampGroup);

        for (int i = logs.size()-1; i >= 0; i--) {
            String line = logs.get(i);
            Matcher match = timestampPattern.matcher(line);
            if (match.find()) {
                String currTimestamp = match.group("timestamp");
                if (lastReceivedLogDateText.equals(currTimestamp)) {
                    pos = i;
                    break;
                }
            }
        }

        if (pos == 0) {
            // this should not happen
            return logs;
        }
        // reflected list
        return logs.subList(pos, logs.size());
    }

    private List<LogSample> convertToDomainModel(List<String> logs, String logDateFormat, String logDatePattern) {

        String namedTimestampGroup = String.format(TIMESTAMP_GROUP, logDatePattern);
        Pattern timestampPattern = Pattern.compile(namedTimestampGroup);
        SimpleDateFormat formatter = new SimpleDateFormat(logDateFormat);

        List<LogSample> list = new ArrayList<>();
        for (String line : logs) {
            Matcher match = timestampPattern.matcher(line);
            if (match.find()) {
                String timestamp = match.group("timestamp");
                Date timestampDate = null;
                try {
                    timestampDate = formatter.parse(timestamp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                list.add(new LogSample(timestampDate, line));
            }
        }

        return list;
    }

    public List<LogSample> prepareLogsToInsert(List<String> logs, Date lastReceivedLogDate, String logDateFormat, String logDatePattern) {

        List<String> filtered = filterLatestLogs(logs, lastReceivedLogDate, logDateFormat, logDatePattern);
        return convertToDomainModel(filtered, logDateFormat, logDatePattern);
    }
}
