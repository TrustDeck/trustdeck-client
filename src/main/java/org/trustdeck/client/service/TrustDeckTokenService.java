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

package org.trustdeck.client.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.trustdeck.client.config.TrustDeckClientConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe helper class to manage token retrieval for accessing TrustDeck.
 * 
 * @author Armin Müller
 */
@Slf4j
public class TrustDeckTokenService {

	/** Keycloak token manager object that allows multi-threaded access and refreshing of the token. */
    private volatile TokenManager trustDeckTokenManager;
    
    /** Object used to set a lock to avoid race-conditions when initializing or refreshing the token. */
    private final Object tokenLock = new Object();

    /** Enables access to the TrustDeck configuration. */
    private TrustDeckClientConfig trustDeckClientConfig;
    
    /**
     * Constructor that also initializes the token manager.
     * 
     * @param config the configuration for this instance
     */
    public TrustDeckTokenService(TrustDeckClientConfig config) {
    	this.trustDeckClientConfig = config;
    	initializeTokenManager();
    }

    /**
     * This method returns a valid access token for authentication 
     * against TrustDeck in a thread-safe manner.
     * It refreshes the token if necessary.
     * 
     * @return a valid access token as a String
     */
	public String authenticate() {
        // Ensure that the token manager can produce a non-expired access token
        refreshTokenIfNecessary();
        
        // Retrieve access token
        String accessToken = trustDeckTokenManager.getAccessTokenString();
        
        log.trace("Retrieved token to authenticate against TrustDeck: [" + accessToken + "]");
        return accessToken;
    }

    /**
     * This method ensures that there is a token manager object available (thread-safe).
     */
    private void initializeTokenManager() {
        // To initialize the token manager in a thread-safe way
    	// (and to not initialize it twice), we use double-checked locking
    	if (trustDeckTokenManager == null) {
            synchronized (tokenLock) {
            	// Second check to ensure that in between the first check and 
            	// locking, no other thread has created the token manager
                if (trustDeckTokenManager == null) {
                    Keycloak keycloak = KeycloakBuilder.builder()
                    		.serverUrl(trustDeckClientConfig.getKeycloakUrl())
                            .realm(trustDeckClientConfig.getRealm())
                            .clientId(trustDeckClientConfig.getClientId())
                            .clientSecret(trustDeckClientConfig.getClientSecret())
                            .username(trustDeckClientConfig.getUserName())
                            .password(trustDeckClientConfig.getPassword())
                            .grantType("password")
                            .build();
                    trustDeckTokenManager = keycloak.tokenManager();
                }
            }
        }
    }

    /**
     * If the current access token is about to expire or already expired,
     * this method refreshes the token object in the token manager in a
     * thread-safe way.
     */
    private void refreshTokenIfNecessary() {
        synchronized (tokenLock) {
        	// If the remaining life time is less than 60 seconds, refresh the token object in the token manager
            if (trustDeckTokenManager.getAccessToken().getExpiresIn() <= 60L) {
                try {
                    trustDeckTokenManager.refreshToken();
                    log.trace("The token object for TrustDeck was refreshed successfully.");
                } catch (Exception e) {
                    throw new RuntimeException("Failed to refresh Keycloak token.", e);
                }
            }
        }
    }
}

