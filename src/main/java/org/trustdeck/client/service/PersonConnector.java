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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.model.Person;
import org.trustdeck.client.util.TrustDeckClientUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * A connector library for programmatic interaction with the person management endpoints
 * of the KING registration service in TrustDeck.
 * Provides methods for person operations (create, read, update, delete).
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Slf4j
@Component
public class PersonConnector {

	/** Enables access to the configuration variables. */
	@Autowired
	private TrustDeckClientConfig trustDeckClientConfig;
	
	/** Enables access to utility methods. */
	@Autowired
	private TrustDeckClientUtil util;

    /**
     * Creates a new person object.
     * 
     * @param person the person object to send
     * @return an empty response
     */
    public ResponseEntity<Void> createPerson(Person person) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                    .path("api/registration/person")
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(person), Void.class);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to create person: " + e.getMessage(), e);
        }
    }

    //TODO: add all methods
 }