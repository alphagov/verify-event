package uk.gov.ida.event;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import uk.gov.ida.event.configuration.DatabaseConfiguration;
import uk.gov.ida.event.configuration.EventConfiguration;
import uk.gov.ida.event.configuration.EventEmitterConfiguration;
import uk.gov.ida.eventemitter.Configuration;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class EventModule extends AbstractModule {

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    private AWSKMS getAmazonKms(
        final AWSCredentials credentials,
        final EventConfiguration configuration) {

        return AWSKMSClientBuilder
                   .standard()
                   .withCredentials(new AWSStaticCredentialsProvider(credentials))
                   .withRegion(configuration.getEventEmitterConfiguration().getRegion())
                   .build();
    }

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    private Configuration getEventEmitterConfiguration(final EventConfiguration configuration,
                                                       final AWSKMS amazonKms) throws UnsupportedEncodingException {

        return new EventEmitterConfiguration(
                configuration.getEventEmitterConfiguration().isEnabled(),
                configuration.getEventEmitterConfiguration().getAccessKeyId(),
                configuration.getEventEmitterConfiguration().getSecretAccessKey(),
                configuration.getEventEmitterConfiguration().getRegion(),
                decrypt(configuration.getEventEmitterConfiguration().getEncryptionKey(), amazonKms),
                configuration.getEventEmitterConfiguration().getApiGatewayUrl(),
                configuration.getEventEmitterConfiguration().getAssumeRole(),
                configuration.getEventEmitterConfiguration().getSessionToken());
    }

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    private DatabaseConfiguration getDatabaseConfiguration(final EventConfiguration configuration,
                                                           final AWSKMS amazonKms) throws UnsupportedEncodingException {
        return new DatabaseConfiguration(
            configuration.getDatabaseConfiguration().getRdsInstanceHostname(),
            configuration.getDatabaseConfiguration().getRdsInstancePort(),
            configuration.getDatabaseConfiguration().getUserName(),
            configuration.getDatabaseConfiguration().getPassword(),
            configuration.getDatabaseConfiguration().getDatabase(),
            decrypt(configuration.getDatabaseConfiguration().getDatabaseTruststorePassword(), amazonKms)
        );
    }

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    private Database getDatabase(final DatabaseConfiguration databaseConfiguration, final ObjectMapper objectMapper) {
        return new Database(databaseConfiguration, objectMapper);
    }

    @Provides
    @Singleton
    @SuppressWarnings("unused")
    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.setDateFormat(new StdDateFormat());
        return mapper;
    }

    private String decrypt(final String encryptedText,
                           final AWSKMS amazonKms) throws UnsupportedEncodingException {
        final DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(Base64.decode(encryptedText)));
        final DecryptResult key = amazonKms.decrypt(request);
        return new String(key.getPlaintext().array(), "UTF-8");
    }

    private String decrypt(final byte[] encryptedText,
                           final AWSKMS amazonKms) throws UnsupportedEncodingException {
        final DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(encryptedText));
        final DecryptResult key = amazonKms.decrypt(request);
        return new String (Base64.encode(key.getPlaintext().array()), "UTF-8");
    }
}
