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

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.exception.TrustDeckClientLibraryException;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.IdentifierItem;
import org.trustdeck.client.model.Person;
import org.trustdeck.client.util.TrustDeckRequestUtil;

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
	private TrustDeckRequestUtil util;

	/**
	 * Constructor for a connector handling person-specific requests.
	 * Initializes the config and the utility object.
	 * 
	 * @param config the configuration for this TrustDeck connection
	 * @param trustDeckRequestUtil the helper object handling authentication and some request building tasks 
	 */
	public Persons(TrustDeckClientConfig config, TrustDeckRequestUtil trustDeckRequestUtil) {
		this.trustDeckClientConfig = config;
		this.util = trustDeckRequestUtil;
	}

    /**
     * Creates a new person object.
     * 
     * @param person the person object containing the information necessary to create a new person in TrustDeck
     * @return the created person object when the creation was successful, {@code null} when a conflicting person object was encountered
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Person create(Person person) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/registration/person")
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Person> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(person), Person.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Creating person failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.CREATED) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		throw new TrustDeckResponseException("Either the first name, the last name, or the administrative gender was missing.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.CONFLICT) {
    		log.debug("The person that was to be inserted was already in the database.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		throw new TrustDeckResponseException("Creating the person (or the associated algorithm) failed.", response.getStatusCode());
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Searches for person objects in TrustDeck.
     * 
     * @param query the term to search for
     * @return a list of persons found, or {@code null} when unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public List<Person> search(String query) throws TrustDeckClientLibraryException, TrustDeckResponseException {
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
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Searching person failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return Arrays.asList(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.PARTIAL_CONTENT) {
    		log.debug("The search for persons returned too many results and was therefore truncated.");
    		return Arrays.asList(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The query found no persons.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Retrieves a person identified by its identifier and idType.
     * 
     * @param identifierItem the identifier and idType of the person of interest
     * @return the retrieved person object, or {@code null} when nothing was found
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Person get(IdentifierItem identifierItem) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/registration/person")
                .queryParam("identifier", identifierItem.getIdentifier())
                .queryParam("idType", identifierItem.getIdType())
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Person> response = null;
    	try {
            response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Person.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Retrieving person failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		throw new TrustDeckResponseException("Either identifier or idType (or both) were missing.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The requested person was not found.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }
    
    /**
     * Updates a person object in TrustDeck.
     * 
     * @param identifierItem identifier and idType data of the person that should be updated
     * @param updatedPerson a person object containing all updated information 
     * 			(everything that is not provided will not be updated and will be kept as is)
     * @return the updated person object if the update was successful, {@code null} if it failed
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Person update(IdentifierItem identifierItem, Person updatedPerson) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/registration/person")
                .queryParam("identifier", identifierItem.getIdentifier())
                .queryParam("idType", identifierItem.getIdType())
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Person> response = null;
    	try {
            response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(updatedPerson), Person.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Updating person failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The person that should be updated could not be found in TrustDeck.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Updating the person failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }
    
    /**
     * Deletes a person from TrustDeck.
     * 
     * @param identifierItem identifier and idType data of the person that should be deleted
     * @return {@code true} if the deletion was successful, {@code false} if not
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public boolean delete(IdentifierItem identifierItem) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/registration/person")
                .queryParam("identifier", identifierItem.getIdentifier())
                .queryParam("idType", identifierItem.getIdType())
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Void> response = null;
    	try {
            response = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Deleting person failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The person that should be deleted could not be found in TrustDeck.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("The deletion would have affected more than one person, so it was aborted.");
    		return false;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }
 }