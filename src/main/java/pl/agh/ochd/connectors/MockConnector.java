package pl.agh.ochd.connectors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MockConnector implements Connector {

    private String path;
    private int offset = 0;
    private int limit = 30;

    public MockConnector(String path) {
        this.path = path;
    }

    @Override
    public Optional<List<String>> getLogs() {

        try {
            Stream<String> stream = Files.lines(Paths.get(path)).skip(offset).limit(limit);
            offset += limit;
            List<String> result = stream.collect(Collectors.toList());
            if (result.size() != 0) {
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
