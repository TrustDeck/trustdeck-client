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

package org.trustdeck.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.exception.TrustDeckClientLibraryException;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.service.Domains;
import org.trustdeck.client.service.Persons;
import org.trustdeck.client.service.Pseudonyms;
import org.trustdeck.client.service.TrustDeckTokenService;
import org.trustdeck.client.util.TrustDeckRequestUtil;

import lombok.Getter;

/**
 * This class encapsulates the connector subclasses.
 * 
 * @author Armin Müller
 */
public class TrustDeckClient {
	
	/** A service handling the authentication. */
	@Getter
	private TrustDeckTokenService tokenService;

	/** Enables access to utility functions. */
	@Getter
	private TrustDeckRequestUtil util;
	
	/** Enables access to the config parameters. */
	private TrustDeckClientConfig config;

	/** Connector for the domain-scope. */
	private Domains domains;

	/** Connector for the person-scope. */
	private Persons persons;

	/**
	 * Constructor initializing all needed sub-connectors.
	 * 
	 * @param config the configuration for this client instance
	 */
	public TrustDeckClient(TrustDeckClientConfig config) {
		this.config = config;
		this.tokenService = new TrustDeckTokenService(config);
		this.util = new TrustDeckRequestUtil(tokenService);
		this.domains = new Domains(config, util);
		this.persons = new Persons(config, util);
	}

	/**
	 * Enables access to API methods for the domain-scope.
	 * 
	 * @return the domain connector
	 */
	public Domains domains() {
		return this.domains;
	}

	/**
	 * Enables access to API methods for the pseudonym-scope.
	 * 
	 * @return the pseudonym connector
	 */
	public Pseudonyms pseudonyms(String domainName) {
		return new Pseudonyms(config, util, domainName);
	}

	/**
	 * Enables access to API methods for the person-scope.
	 * 
	 * @return the person connector
	 */
	public Persons persons() {
		return this.persons;
	}
	
	/**
	 * Method to ping TrustDeck (e.g. to see if it's online/reachable).
	 * 
	 * @return {@code true} if the ping was successful, {@code false} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
	 */
	public boolean ping() throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = config.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/ping")
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Void> response = null;
    	try {
            response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Pinging TrustDeck failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return true;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

}
