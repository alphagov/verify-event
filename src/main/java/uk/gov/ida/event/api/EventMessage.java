package uk.gov.ida.event.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import uk.gov.ida.eventemitter.Event;

import javax.annotation.concurrent.Immutable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Immutable
public final class EventMessage implements Event {

    @JsonProperty
    private UUID eventId;

    @JsonProperty
    private DateTime timestamp = DateTime.now().withZone(DateTimeZone.UTC);

    @JsonProperty
    private String eventType;

    @JsonProperty
    private String originatingService;

    @JsonProperty
    private String sessionId;

    @JsonProperty
    private Map<EventDetailsKey, String> details;

    private EventMessage() {
        // Jackson deserialization
    }

    public EventMessage(
        final UUID eventId,
        final DateTime timestamp,
        final String eventType,
        final String originatingService,
        final String sessionId,
        final Map<EventDetailsKey, String> details) {

        this.eventId = eventId;
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.originatingService = originatingService;
        this.sessionId = sessionId;
        this.details = Maps.newEnumMap(details);
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public DateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    public String getOriginatingService() {
        return originatingService;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Map<EventDetailsKey, String> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EventMessage{");
        sb.append("eventId=").append(eventId);
        sb.append(", timestamp=").append(timestamp);
        sb.append(", eventType='").append(eventType).append('\'');
        sb.append(", originatingService='").append(originatingService).append('\'');
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", details=").append(details);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final EventMessage that = (EventMessage) o;
        return Objects.equals(eventId, that.eventId) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(eventType, that.eventType) &&
               Objects.equals(originatingService, that.originatingService) &&
               Objects.equals(sessionId, that.sessionId) &&
               Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, timestamp, eventType, originatingService, sessionId, details);
    }
}
