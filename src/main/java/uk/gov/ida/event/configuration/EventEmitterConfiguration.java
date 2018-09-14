package uk.gov.ida.event.configuration;

import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.ida.eventemitter.Configuration;

import javax.validation.Valid;
import java.net.URI;
import java.util.Base64;

public final class EventEmitterConfiguration implements Configuration {

    @Valid
    @NotEmpty
    @JsonProperty
    private boolean enabled;

    @Valid
    @NotEmpty
    @JsonProperty
    private String accessKeyId;

    @Valid
    @NotEmpty
    @JsonProperty
    private String secretAccessKey;

    @Valid
    @NotEmpty
    @JsonProperty
    private Regions region;

    @Valid
    @JsonProperty
    private String encryptionKey;

    @Valid
    @JsonProperty
    private URI apiGatewayUrl;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getAccessKeyId() {
        return accessKeyId;
    }

    @Override
    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    @Override
    public Regions getRegion() {
        return region;
    }

    @Override
    public URI getApiGatewayUrl() {
        return apiGatewayUrl;
    }

    @Override
    public byte[] getEncryptionKey() {
        return Base64.getDecoder().decode(encryptionKey);
    }
}
