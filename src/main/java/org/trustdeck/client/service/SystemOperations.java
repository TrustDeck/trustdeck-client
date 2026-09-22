/*
 * TrustDeck Client Library
 * Copyright 2026 Armin Müller
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

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.SystemStatistics;

/**
 * Synchronous operations for global TrustDeck system information.
 *
 * @author Armin Müller
 */
public class SystemOperations {

    /** Shared HTTP transport object. */
    private final TrustDeckHttpClient http;

    /**
     * Creates the service without making an HTTP request.
     *
     * @param http shared HTTP transport object
     */
    public SystemOperations(TrustDeckHttpClient http) {
        this.http = http;
    }

    /**
     * Gets global system statistics.
     *
     * @return typed system statistics
     */
    public SystemStatistics getStatistics() {
        return http.exchange(HttpMethod.GET, http.uri(new String[] { "api", "system", "statistics" }, Map.of()), 
        		null, SystemStatistics.class, Set.of(200), true).getBody();
    }
}
