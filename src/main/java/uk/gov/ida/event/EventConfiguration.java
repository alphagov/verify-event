package uk.gov.ida.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public final class EventConfiguration extends Configuration {

    @JsonProperty
    private EventEmitterConfiguration eventEmitterConfiguration;

    public EventEmitterConfiguration getEventEmitterConfiguration() {
        return eventEmitterConfiguration;
    }
}
