package uk.gov.ida.event.resources;

import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.junit.Test;
import uk.gov.ida.eventemitter.EventEmitter;
import uk.gov.ida.eventemitter.EventMessage;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.ida.eventemitter.EventMessageBuilder.anEventMessage;

public class EventResourceTest {

    static {
        JerseyGuiceUtils.install((s, serviceLocator) -> null);
    }

    private final EventEmitter eventEmitter = mock(EventEmitter.class);
    private final EventResource resource = new EventResource(eventEmitter);

    @Test
    public void shouldReturnOkWhenVerifyEventIsSent() {
        final EventMessage event = anEventMessage().build();

        final Response response = resource.sendEvent(event);

        verify(eventEmitter).record(event);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}
