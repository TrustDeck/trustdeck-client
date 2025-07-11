/*
 * ACE (Advanced Confidentiality Engine) Client Library
 * Copyright 2025 Chethan Chinnabhandara Nagaraj & Armin Müller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustdeck.ace.client.util;

import org.keycloak.admin.client.Keycloak;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.concurrent.locks.ReentrantLock;

public class AceClientUtil {
    private final ReentrantLock lock = new ReentrantLock();

    public HttpHeaders createHeaders(Keycloak keycloakClient) {
        lock.lock();
        try {
            ensureValidTokenOrRefresh(keycloakClient);
            String token = keycloakClient.tokenManager().getAccessTokenString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            return headers;
        } finally {
            lock.unlock();
        }
    }

    public void ensureValidTokenOrRefresh(Keycloak keycloakClient) {
        lock.lock();
        try {
            // Keycloak's token manager handles expiration check and refresh internally
            keycloakClient.tokenManager().getAccessToken();
        } finally {
            lock.unlock();
        }
    }
}