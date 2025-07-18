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

package org.trustdeck.client.config;

import lombok.Getter;

/**
 * Configuration builder class as an alternative way to configure the client.
 * 
 * @author Armin Müller
 */
@Getter
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
	
    /**
     * Basic empty constructor.
     */
    public TrustDeckClientConfig() {
    	// Empty by design
    }
    
	/**
	 * Setter for the service URL for TrustDeck.
	 * 
	 * @param serviceUrl the base URL where TrustDeck is located at
	 * @return the updated TrustDeckClientConfig
	 */
	public TrustDeckClientConfig serviceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        return this;
    }

	/**
	 * Setter for the Keycloak URL.
	 * 
	 * @param keycloakUrl the URL where TrustDeck's Keycloak instance is located at
	 * @return the updated TrustDeckClientConfig
	 */
    public TrustDeckClientConfig keycloakUrl(String keycloakUrl) {
        this.keycloakUrl = keycloakUrl;
        return this;
    }

    /**
     * Setter for the Keycloak realm.
     * 
     * @param realm the name of the Keycloak realm
     * @return the updated TrustDeckClientConfig
     */
    public TrustDeckClientConfig realm(String realm) {
        this.realm = realm;
        return this;
    }

    /**
     * Setter for the client ID.
     * 
     * @param clientId the client ID for authentication against Keycloak
     * @return the updated TrustDeckClientConfig
     */
    public TrustDeckClientConfig clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Setter for the client secret.
     * 
     * @param clientSecret the client secret for authentication against Keycloak
     * @return the updated TrustDeckClientConfig
     */
    public TrustDeckClientConfig clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Setter for the username.
     * 
     * @param userName the username for authentication against Keycloak
     * @return the updated TrustDeckClientConfig
     */
    public TrustDeckClientConfig userName(String userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Setter for the user password.
     * 
     * @param password the user's password for authentication against Keycloak
     * @return the updated TrustDeckClientConfig
     */
    public TrustDeckClientConfig password(String password) {
        this.password = password;
        return this;
    }
}
