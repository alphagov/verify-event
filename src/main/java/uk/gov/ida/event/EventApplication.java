package uk.gov.ida.event;

import com.hubspot.dropwizard.guicier.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.event.health.EventHealthCheck;
import uk.gov.ida.event.resources.EventResource;
import uk.gov.ida.eventemitter.EventEmitterModule;

public class EventApplication extends Application<EventConfiguration> {

    public static void main(String[] args) throws Exception {
        new EventApplication().run(args);
    }

    @Override
    public String getName() {
        return "event";
    }

    @Override
    public void initialize(final Bootstrap<EventConfiguration> bootstrap) {
        GuiceBundle<EventConfiguration> guiceBundle = GuiceBundle.defaultBuilder(EventConfiguration.class)
                                                                 .modules(new EventEmitterModule(), new EventModule())
                                                                 .build();
        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(
        final EventConfiguration configuration,
        final Environment environment) {

        final EventHealthCheck eventHealthCheck = new EventHealthCheck();
        environment.healthChecks().register(eventHealthCheck.getName(), eventHealthCheck);
        environment.jersey().register(EventResource.class);
    }
}
