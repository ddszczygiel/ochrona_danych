package pl.agh.ochd.connectors;

import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.agh.ochd.model.RemoteHost;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class HTTPConnector implements Connector {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPConnector.class);

    private RemoteHost host;
    private OkHttpClient httpClient;

    public HTTPConnector(final RemoteHost host) {

        this.host = host;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (host.getUserName() != null && host.getPasswd() != null) {
            builder.authenticator((route, response) -> {
                // this is triggered when 401 response arrives
                if (responseCount(response) >= 3) {
                    // 3 times authentication failure - stop retrying
                    return null;
                }
                String credential = Credentials.basic(host.getUserName(), host.getPasswd());
                return response.request().newBuilder().header("Authorization", credential).build();
            });
        }

        builder.connectTimeout(10, TimeUnit.SECONDS);
        this.httpClient = builder.build();
    }

    private int responseCount(Response response) {

        Response ref = response;
        int result = 1;
        while ((ref = ref.priorResponse()) != null) {
            result++;
        }

        return result;
    }

    private String prepareRangeHeader(long lastByte) {

        return "bytes=" + lastByte + "-";
    }

    private void setLastReceivedByte(String contentHeader) {

        if (contentHeader == null) {
            return;
        }

        LOGGER.debug("Header Content-Length: " + contentHeader);
        host.setLastReceivedByte(Long.parseLong(contentHeader));
    }

    Optional<List<String>> getLines(String url, Optional<Long> range) {

        LOGGER.debug("Sending request to URL: " + url);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url);
        if (range.isPresent()) {
            LOGGER.debug("Range header present with value: " + range.get());
            requestBuilder.addHeader("Range", prepareRangeHeader(range.get()));
        }

        Request request = requestBuilder.build();
        Response response = null;

        try {
            response = httpClient.newCall(request).execute();
            String content = IOUtils.toString(response.body().byteStream()).trim();
            //TODO check it
            setLastReceivedByte(response.header("Content-Length"));
            return Optional.of(new ArrayList<>(Arrays.asList(content.split("\\n"))));
        } catch (IOException e) {
            LOGGER.error("Error while reading response", e);
            return Optional.empty();
        } finally {
            if (response != null) {
                response.body().close();
            }
        }
    }

    public Optional<List<String>> getLogs() {

        if (isFileRolled(host)) {

            List<String> lines = new ArrayList<>();
            Date yesterday = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
            SimpleDateFormat rolledFormatter = new SimpleDateFormat(host.getOldLogPattern());
            String rolledFileFullPath = host.getLogPath() + rolledFormatter.format(yesterday);

            Optional<List<String>> rolledLines = getLines(rolledFileFullPath, Optional.of(host.getLastReceivedByte()));
            if (rolledLines.isPresent()) {
                lines.addAll(rolledLines.get());
            } else {
                LOGGER.error("Could not retrieve content from rolled file");
                return Optional.empty();
            }

            // now the rest bytes from new file
            String logFullPath = host.getLogPath() + host.getLogFile();
            Optional<List<String>> logLines = getLines(logFullPath, Optional.of(0L));
            if (logLines.isPresent()) {
                lines.addAll(rolledLines.get());
                return lines.isEmpty() ? Optional.empty() : Optional.of(lines);
            } else {
                LOGGER.error("Could not retrieve content from rolled file");
                return Optional.empty();
            }
        } else {

            String logFullPath = host.getLogPath() + host.getLogFile();
            Optional<List<String>> logLines = getLines(logFullPath, Optional.of(host.getLastReceivedByte()));
            if (logLines.isPresent()) {
                return Optional.of(logLines.get());
            } else {
                LOGGER.error("Could not retrieve content from rolled file");
                return Optional.empty();
            }
        }

    }

}
