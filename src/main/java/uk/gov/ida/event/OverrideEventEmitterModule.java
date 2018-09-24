package uk.gov.ida.event;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import uk.gov.ida.event.configuration.EventConfiguration;
import uk.gov.ida.event.configuration.EventEmitterConfiguration;

import javax.annotation.Nullable;
import javax.inject.Singleton;

public class OverrideEventEmitterModule extends AbstractModule {

    private static final int DURATION_SECONDS = 900;

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    @Nullable
    @SuppressWarnings("unused")
    private AWSCredentials getAmazonCredential(final EventConfiguration eventConfiguration) {
        final EventEmitterConfiguration configuration = eventConfiguration.getEventEmitterConfiguration();
        if (configuration.isEnabled()) {
            final BasicSessionCredentials sessionCredentials = new BasicSessionCredentials(
                configuration.getAccessKeyId(),
                configuration.getSecretAccessKey(),
                configuration.getSessionToken());

            final AWSSecurityTokenService amazonSecurityTokenService =
                AWSSecurityTokenServiceClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                    .withRegion(configuration.getRegion())
                    .build();

            final DateTime timestamp = new DateTime();
            final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd_HHmmssSSS");
            final String sessionName = "VerifyEventSession_"+ formatter.print(timestamp);

            final AssumeRoleResult assumeRoleResult =
                amazonSecurityTokenService
                    .assumeRole(new AssumeRoleRequest()
                                    .withRoleArn(configuration.getAssumeRole())
                                    .withRoleSessionName(sessionName)
                                    .withDurationSeconds(DURATION_SECONDS));

            return new BasicSessionCredentials(
                assumeRoleResult.getCredentials().getAccessKeyId(),
                assumeRoleResult.getCredentials().getSecretAccessKey(),
                assumeRoleResult.getCredentials().getSessionToken());
        }
        return null;
    }
}
