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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Configuration builder class as an alternative way to configure the client.
 * 
 * @author Armin Müller
 */
@Component
public class TrustDeckClientConfigBuilder {
	
	/** Enables access to the TrustDeck configuration. */
	@Autowired
	private TrustDeckClientConfig trustDeckConfig;
	
	/**
	 * Setter for the service URL for TrustDeck.
	 * 
	 * @param serviceUrl the base URL where TrustDeck is located at
	 * @return the updated TrustDeckClientConfigBuilder
	 */
	public TrustDeckClientConfigBuilder serviceUrl(String serviceUrl) {
        this.trustDeckConfig.setServiceUrl(serviceUrl);
        return this;
    }

	/**
	 * Setter for the Keycloak URL.
	 * 
	 * @param keycloakUrl the URL where TrustDeck's Keycloak instance is located at
	 * @return the updated TrustDeckClientConfigBuilder
	 */
    public TrustDeckClientConfigBuilder keycloakUrl(String keycloakUrl) {
        this.trustDeckConfig.setKeycloakUrl(keycloakUrl);
        return this;
    }

    /**
     * Setter for the Keycloak realm.
     * 
     * @param realm the name of the Keycloak realm
     * @return the updated TrustDeckClientConfigBuilder
     */
    public TrustDeckClientConfigBuilder realm(String realm) {
        this.trustDeckConfig.setRealm(realm);
        return this;
    }

    /**
     * Setter for the client ID.
     * 
     * @param clientId the client ID for authentication against Keycloak
     * @return the updated TrustDeckClientConfigBuilder
     */
    public TrustDeckClientConfigBuilder clientId(String clientId) {
        this.trustDeckConfig.setClientId(clientId);
        return this;
    }

    /**
     * Setter for the client secret.
     * 
     * @param clientSecret the client secret for authentication against Keycloak
     * @return the updated TrustDeckClientConfigBuilder
     */
    public TrustDeckClientConfigBuilder clientSecret(String clientSecret) {
        this.trustDeckConfig.setClientSecret(clientSecret);
        return this;
    }

    /**
     * Setter for the username.
     * 
     * @param userName the username for authentication against Keycloak
     * @return the updated TrustDeckClientConfigBuilder
     */
    public TrustDeckClientConfigBuilder userName(String userName) {
        this.trustDeckConfig.setUserName(userName);
        return this;
    }

    /**
     * Setter for the user password.
     * 
     * @param password the user's password for authentication against Keycloak
     * @return the updated TrustDeckClientConfigBuilder
     */
    public TrustDeckClientConfigBuilder password(String password) {
        this.trustDeckConfig.setPassword(password);
        return this;
    }
}
