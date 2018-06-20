package uk.gov.ida.event.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.event.api.EventMessageBuilder.anEventMessage;

public class EventMessageTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static EventMessage event_message;

    @Before
    public void setUp() {
        event_message = anEventMessage().withTimestamp(DateTime.parse("2018-06-28T10:50:24+00:00"))
                                        .build();
    }

    @Test
    public void serialisesToJson() throws Exception {
        final String expected = MAPPER.writeValueAsString(
            MAPPER.readValue(fixture("fixtures/eventMessage.json"), EventMessage.class));

        assertThat(MAPPER.writeValueAsString(event_message)).isEqualTo(expected);
    }

    @Test
    public void deserialisesFromJson() throws Exception {
        assertThat(MAPPER.readValue(fixture("fixtures/eventMessage.json"), EventMessage.class))
            .isEqualTo(event_message);
    }

    @Test
    public void equalsContract() {
        EqualsVerifier.forClass(EventMessage.class).verify();
    }
}
