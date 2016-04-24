package pl.agh.ochd.domain;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents the resource (server, vm, application etc.) which is under monitoring.
 */
public class ResourceId {

    private final String value;

    public ResourceId(String value) {

        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Value cannot be null or empty.");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
