package uk.gov.ida.event.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;

public final class DatabaseConfiguration {

    @Valid
    @NotEmpty
    @JsonProperty
    private String rdsInstanceHostname;

    @Valid
    @NotEmpty
    @JsonProperty
    private String rdsInstancePort;

    @Valid
    @NotEmpty
    @JsonProperty
    private String userName;

    @Valid
    @NotEmpty
    @JsonProperty
    private String password;

    @Valid
    @NotEmpty
    @JsonProperty
    private String database;

    @Valid
    @NotEmpty
    @JsonProperty
    private String databaseTruststorePassword;

    private DatabaseConfiguration() {
    }

    public DatabaseConfiguration(final String rdsInstanceHostname,
                                 final String rdsInstancePort,
                                 final String userName,
                                 final String password,
                                 final String database,
                                 final String databaseTruststorePassword) {
        this.rdsInstanceHostname = rdsInstanceHostname;
        this.rdsInstancePort = rdsInstancePort;
        this.userName = userName;
        this.password = password;
        this.database = database;
        this.databaseTruststorePassword = databaseTruststorePassword;
    }

    public String getRdsInstanceHostname() {
        return rdsInstanceHostname;
    }

    public String getRdsInstancePort() {
        return rdsInstancePort;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

    public String getDatabaseTruststorePassword() {
        return databaseTruststorePassword;
    }

    public String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", getRdsInstanceHostname(), getRdsInstancePort(), getDatabase());
    }
}
