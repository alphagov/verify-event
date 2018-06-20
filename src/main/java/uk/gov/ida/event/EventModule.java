package uk.gov.ida.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import uk.gov.ida.eventemitter.Configuration;

public class EventModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    private Configuration getEventEmitterConfiguration(final EventConfiguration configuration) {
        return configuration.getEventEmitterConfiguration();
    }

    @Provides
    @Singleton
    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setDateFormat(new StdDateFormat());
        return mapper;
    }
}
