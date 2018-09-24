package uk.gov.ida.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import uk.gov.ida.event.configuration.DatabaseConfiguration;
import uk.gov.ida.eventemitter.Event;
import uk.gov.ida.eventemitter.EventDetailsKey;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static java.lang.Thread.currentThread;

public class Database {

    private static final String KEY_STORE_TYPE = "JKS";

    {
        try {
            currentThread().getContextClassLoader().loadClass("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private DatabaseConfiguration configuration;
    private ObjectMapper objectMapper;

    public Database(final DatabaseConfiguration configuration,
                    final ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
    }

    public boolean auditEventsTableHasEvent(final Event event) throws SQLException, URISyntaxException, JsonProcessingException {
        int numberOfEvents = 0;
        setSslProperties();
        try (Connection connection = getDatabaseConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT " +
                     "COUNT(1) " +
                     "FROM " +
                     "audit.audit_events " +
                     "WHERE " +
                     "event_id = \'" + event.getEventId().toString() + "\' " +
                     "AND time_stamp = \'" + event.getTimestamp() + "\' " +
                     "AND originating_service = \'" + event.getOriginatingService() + "\' " +
                     "AND session_id = \'" + event.getSessionId() + "\' " +
                     "AND event_type = \'" + event.getEventType() + "\' " +
                     "AND details = \'" +  objectMapper.writeValueAsString(event.getDetails()) + "\';");) {
            while (rs.next()) {
                numberOfEvents = rs.getInt(1);
            }
        }
        clearSslProperties();
        return numberOfEvents == 1;
    }

    public void deleteEventFromAuditEventsTable(final Event event) throws SQLException, URISyntaxException, JsonProcessingException {
        setSslProperties();
        try (Connection connection = getDatabaseConnection();
             Statement stmt = connection.createStatement();) {
            stmt.execute(
                "DELETE  " +
                    "FROM " +
                    "audit.audit_events " +
                    "WHERE " +
                    "event_id = \'" + event.getEventId().toString() + "\' " +
                    "AND time_stamp = \'" + event.getTimestamp() + "\' " +
                    "AND originating_service = \'" + event.getOriginatingService() + "\' " +
                    "AND session_id = \'" + event.getSessionId() + "\' " +
                    "AND event_type = \'" + event.getEventType() + "\' " +
                    "AND details = \'" +  objectMapper.writeValueAsString(event.getDetails()) + "\';");
        }
        clearSslProperties();
    }

    public boolean billingEventsTableHasEvent(final Event event) throws SQLException, URISyntaxException {
        int numberOfEvents = 0;
        setSslProperties();
        try (Connection connection = getDatabaseConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT " +
                     "COUNT(1) " +
                     "FROM " +
                     "billing.billing_events " +
                     "WHERE " +
                     "time_stamp = \'" + event.getTimestamp() + "\' " +
                     "AND session_id = \'" + event.getSessionId() + "\' " +
                     "AND hashed_persistent_id = \'" + event.getDetails().get(EventDetailsKey.pid) + "\' " +
                     "AND " + EventDetailsKey.request_id + " = \'" + event.getDetails().get(EventDetailsKey.request_id) + "\' " +
                     "AND " + EventDetailsKey.idp_entity_id + " = \'" + event.getDetails().get(EventDetailsKey.idp_entity_id) + "\' " +
                     "AND " + EventDetailsKey.minimum_level_of_assurance + " = \'" + event.getDetails().get(EventDetailsKey.minimum_level_of_assurance) + "\' " +
                     "AND " + EventDetailsKey.required_level_of_assurance + " = \'" + event.getDetails().get(EventDetailsKey.required_level_of_assurance) + "\' " +
                     "AND " + EventDetailsKey.provided_level_of_assurance + " = \'" + event.getDetails().get(EventDetailsKey.provided_level_of_assurance) + "\';");) {
            while (rs.next()) {
                numberOfEvents = rs.getInt(1);
            }
        }
        clearSslProperties();
        return numberOfEvents == 1;
    }

    public void deleteEventFromBillingEventsTable(final Event event) throws SQLException, URISyntaxException {
        setSslProperties();
        try (Connection connection = getDatabaseConnection();
             Statement stmt = connection.createStatement();) {
            stmt.execute(
                "DELETE " +
                "FROM " +
                "billing.billing_events " +
                "WHERE " +
                "time_stamp = \'" + event.getTimestamp() + "\' " +
                "AND session_id = \'" + event.getSessionId() + "\' " +
                "AND hashed_persistent_id = \'" + event.getDetails().get(EventDetailsKey.pid) + "\' " +
                "AND " + EventDetailsKey.request_id + " = \'" + event.getDetails().get(EventDetailsKey.request_id) + "\' " +
                "AND " + EventDetailsKey.idp_entity_id + " = \'" + event.getDetails().get(EventDetailsKey.idp_entity_id) + "\' " +
                "AND " + EventDetailsKey.minimum_level_of_assurance + " = \'" + event.getDetails().get(EventDetailsKey.minimum_level_of_assurance) + "\' " +
                "AND " + EventDetailsKey.required_level_of_assurance + " = \'" + event.getDetails().get(EventDetailsKey.required_level_of_assurance) + "\' " +
                "AND " + EventDetailsKey.provided_level_of_assurance + " = \'" + event.getDetails().get(EventDetailsKey.provided_level_of_assurance) + "\';");
        }
        clearSslProperties();
    }

    public boolean fraudEventsTableHasEvent(final Event event) throws SQLException, URISyntaxException {
        int numberOfEvents = 0;
        setSslProperties();
        try (Connection connection = getDatabaseConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT " +
                 "COUNT(1) " +
                 "FROM " +
                 "billing.fraud_events " +
                 "WHERE " +
                 "time_stamp = \'" + event.getTimestamp() + "\' " +
                 "AND session_id = \'" + event.getSessionId() + "\' " +
                 "AND hashed_persistent_id = \'" + event.getDetails().get(EventDetailsKey.pid) + "\' " +
                 "AND " + EventDetailsKey.request_id + " = \'" + event.getDetails().get(EventDetailsKey.request_id) + "\' " +
                 "AND entity_id = \'" + event.getDetails().get(EventDetailsKey.idp_entity_id) + "\' " +
                 "AND fraud_event_id = \'" + event.getDetails().get(EventDetailsKey.idp_fraud_event_id) + "\' " +
                 "AND fraud_indicator = \'" + event.getDetails().get(EventDetailsKey.gpg45_status) + "\';");) {
            while (rs.next()) {
                numberOfEvents = rs.getInt(1);
            }
        }
        clearSslProperties();
        return numberOfEvents == 1;
    }

    public boolean deleteEventFromFraudEventsTable(final Event event) throws SQLException, URISyntaxException {
        int numberOfEvents = 0;
        setSslProperties();
        try (Connection connection = getDatabaseConnection();
             Statement stmt = connection.createStatement();) {
            stmt.execute(
                "DELETE " +
                "FROM " +
                "billing.fraud_events " +
                "WHERE " +
                "time_stamp = \'" + event.getTimestamp() + "\' " +
                "AND session_id = \'" + event.getSessionId() + "\' " +
                "AND hashed_persistent_id = \'" + event.getDetails().get(EventDetailsKey.pid) + "\' " +
                "AND " + EventDetailsKey.request_id + " = \'" + event.getDetails().get(EventDetailsKey.request_id) + "\' " +
                "AND entity_id = \'" + event.getDetails().get(EventDetailsKey.idp_entity_id) + "\' " +
                "AND fraud_event_id = \'" + event.getDetails().get(EventDetailsKey.idp_fraud_event_id) + "\' " +
                "AND fraud_indicator = \'" + event.getDetails().get(EventDetailsKey.gpg45_status) + "\';");
        }
        clearSslProperties();
        return numberOfEvents == 1;
    }

    private Connection getDatabaseConnection() throws SQLException, URISyntaxException {
        setSslProperties();
        return DriverManager.getConnection(configuration.getJdbcUrl(), setDatabaseConnectionProperties());
    }

    private Properties setDatabaseConnectionProperties() {
        Properties connectionProperties = new Properties();
        connectionProperties.setProperty("verifyServerCertificate", "true");
        connectionProperties.setProperty("useSSL", "true");
        connectionProperties.setProperty("user", configuration.getUserName());
        connectionProperties.setProperty("password", configuration.getPassword());
        return connectionProperties;
    }

    private void setSslProperties() throws URISyntaxException {
        System.setProperty("javax.net.ssl.trustStore",  new File(Resources.getResource("database_truststore.jks").toURI()).getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStoreType", KEY_STORE_TYPE);
        System.setProperty("javax.net.ssl.trustStorePassword", configuration.getDatabaseTruststorePassword());
    }

    private void clearSslProperties() {
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStoreType");
        System.clearProperty("javax.net.ssl.trustStorePassword");
    }
}
