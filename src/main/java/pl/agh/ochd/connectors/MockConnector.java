package pl.agh.ochd.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author majaschaefer
 */
public class MockConnector implements Connector {


    private static final Logger LOGGER = LoggerFactory.getLogger(MockConnector.class);

    private String path;
    private boolean firstTime = true;
    private int offset = 0;


    public MockConnector(String path) {
        this.path = path;


    }

    @Override
    public Optional<List<String>> getLogs() {
        try {
            Stream<String> stream = Files.lines(Paths.get(path)).skip(offset);
            if(firstTime) {
                stream = stream.limit(100);
                firstTime = false;
                offset = offset + 100;

            } else {
                stream = stream.limit(20);
                offset = offset + 20;
            }

            List<String> result = stream.collect(Collectors.toList());
            if(result.size() != 0) {
                return Optional.of(result);
            } else {
                return Optional.empty();
            }

        } catch (IOException e) {
            LOGGER.error("Error while reading from file!",e);
            return Optional.empty();
        }

    }
}
