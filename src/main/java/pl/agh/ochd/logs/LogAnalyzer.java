package pl.agh.ochd.logs;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogAnalyzer {

    private static final String TIMESTAMP_GROUP = "(?<timestamp>%s)";

    public List<String> filterLastestLogs(List<String> logs, String lastLogTimestamp, String logTimestampFormat) {

        int pos = 0;
        String namedTimestampGroup = String.format(TIMESTAMP_GROUP, logTimestampFormat);
        Pattern timestampPattern = Pattern.compile(namedTimestampGroup);
        for (int i = logs.size()-1; i >= 0; i--) {
            String line = logs.get(i);
            Matcher match = timestampPattern.matcher(line);
            if (match.find()) {
                String currTimestamp = match.group("timestamp");
                if (lastLogTimestamp.equals(currTimestamp)) {
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

    // key is line timestamp ( should be date ?? ) second is complete line
    public Map<String, String> getTimeFittedLinesXD(List<String> logs, String logTimestampFormat) {

        String namedTimestampGroup = String.format(TIMESTAMP_GROUP, logTimestampFormat);
        Pattern timestampPattern = Pattern.compile(namedTimestampGroup);
        Map<String, String> map = new HashMap<>();
        for (String line : logs) {
            Matcher match = timestampPattern.matcher(line);
            if (match.find()) {
                map.put(match.group("timestamp"), line);
            }
        }

        return map;
    }

    public Map<String, String> prepareLogsToInsert(List<String> logs, String lastLogTimestamp, String logTimestampFormat) {

        List<String> filtered = filterLastestLogs(logs, lastLogTimestamp, logTimestampFormat);
        return getTimeFittedLinesXD(filtered, logTimestampFormat);
    }
}
