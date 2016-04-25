package pl.agh.ochd.connectors;

import okhttp3.*;
import org.apache.commons.io.IOUtils;
import pl.agh.ochd.model.RemoteHost;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


public class HTTPConnector implements Connector {

    private RemoteHost host;
    private OkHttpClient httpClient;

    public HTTPConnector(final RemoteHost host) {

        this.host = host;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (host.getUserName() != null && host.getPasswd() != null) {
            builder.authenticator(new Authenticator() {

                public Request authenticate(Route route, Response response) throws IOException {
                    // this is triggered when 401 response arrives
                    if (responseCount(response) >= 3) {
                        // 3 times authentication failure - stop retrying
                        return null;
                    }
                    String credential = Credentials.basic(host.getUserName(), host.getPasswd());
                    return response.request().newBuilder().header("Authorization", credential).build();
                }
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

    public Optional<List<String>> getLogs() {

        Request request = new Request.Builder()
                .url(host.getLogPath()+host.getLogFile())
//                .addHeader("BlaBla", "trololo")
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            String file = IOUtils.toString(response.body().byteStream());
            return Optional.of(new ArrayList<>(Arrays.asList(file.split("\\n"))));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (response != null) {
                response.body().close();
            }
        }
    }

}
