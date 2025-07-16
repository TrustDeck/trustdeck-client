/*
 * Trust Deck Client Library
 * Copyright 2025 TrustDeck Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustdeck.ace.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration parameters for the TrustDeck client.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "trustdeck-client")
public class TrustDeckClientConfig {

	/** The base URL of the TrustDeck instance to work with. */
    private String serviceUrl;

	/** The URL of the Keycloak authentication server for this TrustDeck instance. */
    private String keycloakUrl;

	/** The name of the Keycloak realm. */
    private String realm;

	/** The client ID for authenticating against Keycloak. */
    private String clientId;

	/** The client secret for authenticating against Keycloak. */
    private String clientSecret;

	/** The user name for authenticating against Keycloak. */
    private String userName;

	/** The user's password for authenticating against Keycloak. */
    private String password;
}
