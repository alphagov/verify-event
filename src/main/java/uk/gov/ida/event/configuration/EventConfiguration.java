package uk.gov.ida.event.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public final class EventConfiguration extends Configuration {

    @JsonProperty
    private EventEmitterConfiguration eventEmitterConfiguration;

    @JsonProperty
    private DatabaseConfiguration databaseConfiguration;

    public EventEmitterConfiguration getEventEmitterConfiguration() {
        return eventEmitterConfiguration;
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        return databaseConfiguration;
    }
}
