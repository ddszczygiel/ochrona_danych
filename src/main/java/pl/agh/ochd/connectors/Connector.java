package pl.agh.ochd.connectors;


import java.util.List;
import java.util.Optional;

public interface Connector {

    Optional<List<String>> getLogs();
}
