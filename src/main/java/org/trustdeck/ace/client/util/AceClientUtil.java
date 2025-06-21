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
package org.trustdeck.ace.client.util;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.HttpHeaders;

/**
 * A helper class to manage access token fetching, validation, and HTTP header creation
 * for Keycloak authentication in the ACE client library.
 */
@Slf4j
public class AceClientUtil {

    private volatile String accessToken;
    private volatile long tokenExpirationTime;

    /**
     * Creates HTTP headers with authentication and content type.
     *
     * @param keycloakClient The Keycloak client instance to ensure a valid token
     * @return Configured HttpHeaders
     */
    public HttpHeaders createHeaders(Keycloak keycloakClient) {
        ensureValidTokenOrRefresh(keycloakClient);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        return headers;
    }

    /**
     * Fetches or refreshes the access token from Keycloak.
     *
     * @param keycloakClient The Keycloak client instance to use for token retrieval
     */
    public void fetchOrRefreshToken(Keycloak keycloakClient) {
        try {
            AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();
            log.info("token response {}", tokenResponse);
            this.accessToken = tokenResponse.getToken();
            this.tokenExpirationTime = System.currentTimeMillis() + (tokenResponse.getExpiresIn() * 1000);
            log.info("Access token refreshed successfully. Expires in {} seconds", tokenResponse.getExpiresIn());
        } catch (Exception e) {
            log.error("Failed to refresh access token: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to refresh access token: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the current access token is valid (not null and not expired).
     *
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid() {
        if (accessToken == null) {
            return false;
        }
        return System.currentTimeMillis() < (tokenExpirationTime - 10_000);
    }

    /**
     * Ensures a valid access token is available by fetching or refreshing if necessary.
     *
     * @param keycloakClient The Keycloak client instance to use for token retrieval
     */
    public void ensureValidTokenOrRefresh(Keycloak keycloakClient) {
        if (!isTokenValid()) {
            fetchOrRefreshToken(keycloakClient);
        }
    }
}