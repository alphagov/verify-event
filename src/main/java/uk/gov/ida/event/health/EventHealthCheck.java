package uk.gov.ida.event.health;

import com.codahale.metrics.health.HealthCheck;

public class EventHealthCheck extends HealthCheck {

    public EventHealthCheck() {
        super();
    }

    public String getName() {
        return "Event Health Check";
    }

    @Override
    protected Result check() {
        return Result.healthy();
    }
}
