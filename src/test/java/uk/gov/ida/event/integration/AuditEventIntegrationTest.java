package uk.gov.ida.event.integration;

import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.client.HttpClientConfiguration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.event.Database;
import uk.gov.ida.event.EventApplication;
import uk.gov.ida.event.configuration.EventConfiguration;
import uk.gov.ida.eventemitter.EventDetailsKey;
import uk.gov.ida.eventemitter.EventHasher;
import uk.gov.ida.eventemitter.EventMessage;
import uk.gov.ida.eventemitter.Sha256Util;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.EnumMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.eventemitter.EventMessageBuilder.anEventMessage;

public class AuditEventIntegrationTest {

    @ClassRule
    public static final DropwizardAppRule<EventConfiguration> RULE =
        new DropwizardAppRule<>(EventApplication.class, ResourceHelpers.resourceFilePath("event_test.yml"));

    private static final int WAIT_FOR_RECORDER_TO_COMPLETE_PROCESSING = 75_000;

    private Database database = new Database(RULE.getConfiguration(), RULE.getObjectMapper());
    private static Client client;
    private static EventHasher eventHasher = new EventHasher(new Sha256Util());

    @BeforeClass
    public static void beforeClass() {
        client = createAClient();
    }

    @Test
    public void shouldSaveAnAuditEventToDatabaseOnce() throws Exception {
        final EventMessage auditEventMessage = createAnAuditEventMessage();
        final EventMessage expectedAuditEventMessage = (EventMessage) eventHasher.replacePersistentIdWithHashedPersistentId(auditEventMessage);

        final Response response1 = client.target(String.format("http://localhost:%d/send-event", RULE.getLocalPort()))
                                        .request(MediaType.APPLICATION_JSON_TYPE)
                                        .post(Entity.json(auditEventMessage));

        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        final Response response2 = client.target(String.format("http://localhost:%d/send-event", RULE.getLocalPort()))
                                        .request(MediaType.APPLICATION_JSON_TYPE)
                                        .post(Entity.json(auditEventMessage));

        assertThat(response2.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        Thread.sleep(WAIT_FOR_RECORDER_TO_COMPLETE_PROCESSING);

        assertThat(database.auditEventsTableHasEvent(expectedAuditEventMessage)).isTrue();
        assertThat(database.billingEventsTableHasEvent(expectedAuditEventMessage)).isFalse();
        assertThat(database.fraudEventsTableHasEvent(expectedAuditEventMessage)).isFalse();
    }

    @Test
    public void shouldSaveABillingEventToDatabase() throws Exception {
        final EventMessage billingEventMessage = createABillingEventMessage();
        final EventMessage expectedBillingEventMessage = (EventMessage) eventHasher.replacePersistentIdWithHashedPersistentId(billingEventMessage);

        final Response response = client.target(String.format("http://localhost:%d/send-event", RULE.getLocalPort()))
                                        .request(MediaType.APPLICATION_JSON_TYPE)
                                        .post(Entity.json(billingEventMessage));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        Thread.sleep(WAIT_FOR_RECORDER_TO_COMPLETE_PROCESSING);

        assertThat(database.auditEventsTableHasEvent(expectedBillingEventMessage)).isTrue();
        assertThat(database.billingEventsTableHasEvent(expectedBillingEventMessage)).isTrue();
        assertThat(database.fraudEventsTableHasEvent(expectedBillingEventMessage)).isFalse();
    }

    @Test
    public void shouldSaveAFraudEventToDatabase() throws Exception {
        final EventMessage fraudEventMessage = createAFraudEventMessage();
        final EventMessage expectedFraudEventMessage = (EventMessage) eventHasher.replacePersistentIdWithHashedPersistentId(fraudEventMessage);

        final Response response = client.target(String.format("http://localhost:%d/send-event", RULE.getLocalPort()))
                                        .request(MediaType.APPLICATION_JSON_TYPE)
                                        .post(Entity.json(fraudEventMessage));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        Thread.sleep(WAIT_FOR_RECORDER_TO_COMPLETE_PROCESSING);

        assertThat(database.auditEventsTableHasEvent(expectedFraudEventMessage)).isTrue();
        assertThat(database.billingEventsTableHasEvent(expectedFraudEventMessage)).isFalse();
        assertThat(database.fraudEventsTableHasEvent(expectedFraudEventMessage)).isTrue();
    }

    private EventMessage createAnAuditEventMessage() {
        final EnumMap<EventDetailsKey, String> details = new EnumMap<>(EventDetailsKey.class);
        details.put(EventDetailsKey.session_event_type, "session_started");
        details.put(EventDetailsKey.session_expiry_time, DateTime.now(DateTimeZone.UTC).plusHours(1).toString());
        details.put(EventDetailsKey.transaction_entity_id, "transaction entity id");
        details.put(EventDetailsKey.minimum_level_of_assurance, "LEVEL_1");
        details.put(EventDetailsKey.required_level_of_assurance, "LEVEL_1");
        details.put(EventDetailsKey.message_id, UUID.randomUUID().toString());
        details.put(EventDetailsKey.principal_ip_address_as_seen_by_hub, "3.3.3.9, 9.9.9.9");
        details.put(EventDetailsKey.request_id, UUID.randomUUID().toString());

        return anEventMessage().withEventId(UUID.randomUUID())
                               .withSessionId(UUID.randomUUID().toString())
                               .withDetails(details)
                               .build();
    }

    private EventMessage createABillingEventMessage() {
        final EnumMap<EventDetailsKey, String> details = new EnumMap<>(EventDetailsKey.class);
        details.put(EventDetailsKey.pid, UUID.randomUUID().toString());
        details.put(EventDetailsKey.request_id, UUID.randomUUID().toString());
        details.put(EventDetailsKey.idp_entity_id, "Identity Provider Entity Id");
        details.put(EventDetailsKey.session_event_type, "idp_authn_succeeded");
        details.put(EventDetailsKey.session_expiry_time, DateTime.now(DateTimeZone.UTC).plusHours(1).toString());
        details.put(EventDetailsKey.transaction_entity_id, "transaction entity id");
        details.put(EventDetailsKey.minimum_level_of_assurance, "LEVEL_1");
        details.put(EventDetailsKey.required_level_of_assurance, "LEVEL_1");
        details.put(EventDetailsKey.provided_level_of_assurance, "LEVEL_1");
        details.put(EventDetailsKey.principal_ip_address_as_seen_by_hub, "3.3.3.9, 9.9.9.9");
        details.put(EventDetailsKey.principal_ip_address_as_seen_by_idp, "3.9.9.9, 9.9.9.9");

        return anEventMessage().withEventId(UUID.randomUUID())
                               .withSessionId(UUID.randomUUID().toString())
                               .withDetails(details)
                               .build();
    }

    private EventMessage createAFraudEventMessage() {
        final EnumMap<EventDetailsKey, String> details = new EnumMap<>(EventDetailsKey.class);
        details.put(EventDetailsKey.pid, UUID.randomUUID().toString());
        details.put(EventDetailsKey.request_id, UUID.randomUUID().toString());
        details.put(EventDetailsKey.gpg45_status, "AA01");
        details.put(EventDetailsKey.idp_entity_id, "Identity Provider Entity Id");
        details.put(EventDetailsKey.idp_fraud_event_id, UUID.randomUUID().toString());
        details.put(EventDetailsKey.session_event_type, "fraud_detected");
        details.put(EventDetailsKey.session_expiry_time, DateTime.now(DateTimeZone.UTC).plusHours(1).toString());
        details.put(EventDetailsKey.transaction_entity_id, "transaction entity id");
        details.put(EventDetailsKey.principal_ip_address_as_seen_by_hub, "3.3.3.9, 9.9.9.9");
        details.put(EventDetailsKey.principal_ip_address_as_seen_by_idp, "3.9.9.9, 9.9.9.9");

        return anEventMessage().withEventId(UUID.randomUUID())
                               .withSessionId(UUID.randomUUID().toString())
                               .withDetails(details)
                               .build();
    }

    private static Client createAClient() {
        HttpClientConfiguration httpClientConfiguration = new HttpClientConfiguration();
        httpClientConfiguration.setTimeout(Duration.milliseconds(4_000));
        HttpClientBuilder clientBuilder = new HttpClientBuilder(RULE.getEnvironment());
        clientBuilder.using(httpClientConfiguration);
        JerseyClientBuilder builder = new JerseyClientBuilder(RULE.getEnvironment());
        builder.setApacheHttpClientBuilder(clientBuilder);
        return builder.build("test client");
    }
}
