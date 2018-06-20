package uk.gov.ida.event.api;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class EventMessageBuilder {

    private UUID eventId = UUID.fromString("7d20e892-2f01-4abc-b575-77b9f93c316f");
    private DateTime timestamp = DateTime.now().withZone(DateTimeZone.UTC);
    private String eventType = "session_event";
    private String originatingService = "test";
    private String sessionId = "f81176c8-24f7-4622-ac61-7ebbc978bcdb";
    private Map<EventDetailsKey, String> details= new HashMap<>();
    {
        details.put(EventDetailsKey.message, "Session error");
        details.put(EventDetailsKey.error_id, "7d20e892-2f01-4abc-b575-77b9f93c316z");
    }

    public static EventMessageBuilder anEventMessage() {
        return new EventMessageBuilder();
    }

    public EventMessage build() {
        return new EventMessage(
            eventId,
            timestamp,
            eventType,
            originatingService,
            sessionId,
            details);
    }

    public EventMessageBuilder withEventId(final UUID eventId) {
        this.eventId = eventId;
        return this;
    }

    public EventMessageBuilder withTimestamp(final DateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public EventMessageBuilder withOriginatingService(final String originatingService) {
        this.originatingService = originatingService;
        return this;
    }

    public EventMessageBuilder withSessionId(final String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public EventMessageBuilder withDetails(final Map<EventDetailsKey, String> details) {
        this.details = details;
        return this;
    }
}
