package pl.agh.ochd.infrastructure;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.agh.ochd.domain.LogSample;
import pl.agh.ochd.domain.ResourceId;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


public class ElasticsearchPersistenceService implements PersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistenceService.class);
    private final Client client;

    public ElasticsearchPersistenceService(String host, int port) throws UnknownHostException {
        client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
    }

    @Override
    public void saveLogs(ResourceId id, Collection<LogSample> logs) {

        logs.forEach(sample -> {
            XContentBuilder builder = null;
            try {
                builder = jsonBuilder()
                        .startObject()
                        .field("timestamp", sample.getTime())
                        .field("message", sample.getMessage())
                        .endObject();

                client.prepareIndex("logs", id.getValue())
                        .setSource(builder)
                        .get();

            } catch (IOException e) {
                LOGGER.error("Error while persisting sample.", e);
            }
        });

    }

    @Override
    public Collection<LogSample> loadLogs(ResourceId id, Date from, Date to, Optional<Pattern> patternOption) {

        SearchRequestBuilder query = client.prepareSearch("logs")
                .setTypes(id.getValue())
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.rangeQuery("timestamp").from(from).to(to));

        if (patternOption.isPresent()) {
            query.setQuery(QueryBuilders.regexpQuery("message", patternOption.get().toString()));
        }

        SearchResponse searchResponse = query.execute().actionGet();
        ArrayList<LogSample> samples = new ArrayList<>();

        for (SearchHit hit : searchResponse.getHits().getHits()) {
            String timestamp = (String) hit.getSource().get("timestamp");
            String message = (String) hit.getSource().get("message");
            Date time = Date.from(LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(timestamp)).atZone(ZoneId.systemDefault()).toInstant());

            samples.add(new LogSample(time, message));
        }
        return samples;
    }

    public void close() {
        client.close();
    }
}
