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

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.model.Person;
import org.trustdeck.client.util.TrustDeckClientUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * A connector library for programmatic interaction with the person management endpoints
 * of the KING registration service in TrustDeck.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Slf4j
public class Persons {

	/** Enables access to the configuration variables. */
	private TrustDeckClientConfig trustDeckClientConfig;
	
	/** Enables access to utility methods. */
	private TrustDeckClientUtil util;

	/**
	 * Constructor for a connector handling person-specific requests.
	 * Initializes the config and the utility object.
	 * 
	 * @param config the configuration for this TrustDeck connection
	 * @param trustDeckClientUtil the helper object handling authentication and some request building tasks 
	 */
	public Persons(TrustDeckClientConfig config, TrustDeckClientUtil trustDeckClientUtil) {
		this.trustDeckClientConfig = config;
		this.util = trustDeckClientUtil;
	}

    /**
     * Creates a new person object.
     * 
     * @param person the person object to send
     * @return {@code true} when the creation was successful, {@code false} otherwise
     */
    public boolean createPerson(Person person) {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/registration/person")
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Void> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(person), Void.class);
        } catch (RestClientException e) {
            log.error("Creating person failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.CREATED) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		log.debug("Either the first name, the last name, or the administrative gender was missing.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.CONFLICT) {
    		log.debug("The person that was to be inserted was already in the database.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Creating the person (or the associated algorithm) failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }

    /**
     * Searches for person objects in TrustDeck.
     * 
     * @param query the term to search for
     * @return a list of persons found, or {@code null} when unsuccessful
     */
    public List<Person> searchPersons(String query) {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/registration/person")
                .queryParam("q", query)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Person[]> response = null;
    	try {
            response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Person[].class);
        } catch (RestClientException e) {
            log.error("Creating person failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return List.of(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.PARTIAL_CONTENT) {
    		log.debug("The search for persons returned too many results and was therefore truncated.");
    		return List.of(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The query found no persons.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    //TODO: add all methods
 }