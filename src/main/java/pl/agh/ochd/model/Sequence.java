package pl.agh.ochd.model;


import com.typesafe.config.Config;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Sequence {

    private String name;
    private List<Pattern> patterns;

    public Sequence(Config config) {

        this.name = config.getString("name");
        this.patterns = preparePatternList(config.getStringList("patterns"));
    }

    public Sequence(String name, List<Pattern> patterns) {
        this.name = name;
        this.patterns = patterns;
    }

    private List<Pattern> preparePatternList(List<String> patterns) {

        return patterns.stream().map(Pattern::compile).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }
}
