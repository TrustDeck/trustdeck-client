package org.trustdeck.ace.client.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for the pseudonymization connector.
 * <p>
 * These properties should be provided by the consuming application
 * (e.g., via constructor or setters).
 * </p>
 */
@Getter
@Setter
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

    /**
     * Constructor to initialize all required properties.
     * @param serviceUrl The URL of the ACE service
     * @param keycloakUrl The URL of the Keycloak server
     * @param realm The Keycloak realm
     * @param clientId The Keycloak client ID
     * @param clientSecret The Keycloak client secret
     * @param userName The Keycloak username
     * @param password The Keycloak password
     */
    public AceClientProperties(String serviceUrl, String keycloakUrl, String realm,
                               String clientId, String clientSecret, String userName, String password) {
        this.serviceUrl = serviceUrl;
        this.keycloakUrl = keycloakUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.userName = userName;
        this.password = password;
    }


    public AceClientProperties() {
    }
}