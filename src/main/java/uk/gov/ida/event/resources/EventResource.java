package uk.gov.ida.event.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.eventemitter.EventEmitter;
import uk.gov.ida.eventemitter.EventMessage;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/send-event")
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventResource.class);

    private final EventEmitter eventEmitter;

    @Inject
    public EventResource(final EventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }

    @POST
    public Response sendEvent(@Valid EventMessage event) {
        LOGGER.info("Received an event: {}", event);
        eventEmitter.record(event);
        return Response.ok().build();
    }
}
