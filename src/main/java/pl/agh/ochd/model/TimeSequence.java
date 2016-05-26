package pl.agh.ochd.model;


import com.typesafe.config.Config;

public class TimeSequence extends Sequence {

    private long interval;

    public TimeSequence(Config config) {

        super(config);
        this.interval = config.getLong("interval");
    }

    public long getInterval() {
        return interval;
    }
}
