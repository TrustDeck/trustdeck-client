package org.trustdeck.ace.client.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the ACE client with a builder pattern.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ace-client")
public class AceClientProperties {

    @NotBlank
    private String serviceUrl;
    @NotBlank
    private String keycloakUrl;
    @NotBlank
    private String realm;
    @NotBlank
    private String clientId;
    @NotBlank
    private String clientSecret;
    @NotBlank
    private String userName;
    @NotBlank
    private String password;

    // Private constructor for builder pattern
    private AceClientProperties(Builder builder) {
        this.serviceUrl = builder.serviceUrl;
        this.keycloakUrl = builder.keycloakUrl;
        this.realm = builder.realm;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.userName = builder.userName;
        this.password = builder.password;
    }

    // Default constructor for Spring Boot binding
    public AceClientProperties() {
    }

    /**
     * Builder class for AceClientProperties.
     */
    public static class Builder {
        private String serviceUrl;
        private String keycloakUrl;
        private String realm;
        private String clientId;
        private String clientSecret;
        private String userName;
        private String password;

        public Builder serviceUrl(String serviceUrl) {
            this.serviceUrl = serviceUrl;
            return this;
        }

        public Builder keycloakUrl(String keycloakUrl) {
            this.keycloakUrl = keycloakUrl;
            return this;
        }

        public Builder realm(String realm) {
            this.realm = realm;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public AceClientProperties build() {
            return new AceClientProperties(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}