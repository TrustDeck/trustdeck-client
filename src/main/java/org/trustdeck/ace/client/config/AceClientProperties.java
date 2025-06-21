/*
 *  * Copyright 2024 Your Organization
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */
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