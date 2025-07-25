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

import lombok.extern.slf4j.Slf4j;

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
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.util.TrustDeckRequestUtil;

/**
 * A connector library for programmatic interaction with the domain management endpoints
 * of the ACE pseudonymization service in TrustDeck.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Slf4j
public class Domains {

	/** Enables access to the configuration variables. */
	private TrustDeckClientConfig trustDeckClientConfig;
	
	/** Enables access to utility methods. */
	private TrustDeckRequestUtil util;

	/**
	 * Constructor for a connector handling domain-specific requests.
	 * Initializes the config and the utility object.
	 * 
	 * @param config the configuration for this TrustDeck connection
	 * @param trustDeckRequestUtil the helper object handling authentication and some request building tasks 
	 */
	public Domains(TrustDeckClientConfig config, TrustDeckRequestUtil trustDeckRequestUtil) {
		this.trustDeckClientConfig = config;
		this.util = trustDeckRequestUtil;
	}
	
    /**
     * Gets a list of all domains.
     *
     * @return a list of domain objects
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public List<Domain> getAll() throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/experimental/domains/hierarchy")
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Domain[]> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Domain[].class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Retrieving all domains failed: " + e.getMessage());
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return Arrays.asList(response.getBody());
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Gets a domain by name.
     *
     * @param domainName the name of the domain
     * @return the requested domain
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Domain get(String domainName) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/domain")
                .queryParam("name", domainName)
                .toUriString();
    	
        // Build and send request
    	ResponseEntity<Domain> response = null;
    	try {
            response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Domain.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Retrieving domain failed: " + e.getMessage());
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain \"" + domainName + "\" was not found.", response.getStatusCode());
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Gets a specific attribute of a domain.
     *
     * @param domainName the name of the domain
     * @param attributeName the name of the attribute to retrieve
     * @return the requested attribute as a String or {@code null} when unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public String getAttribute(String domainName, String attributeName) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .pathSegment("api", "pseudonymization", "domains", domainName, attributeName)
                .toUriString();
    	
        // Build and send request
        ResponseEntity<String> response = null;
    	try {
            response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), String.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Retrieving domain attribute failed: " + e.getMessage());
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain \"" + domainName + "\" was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
    		log.debug("Insufficient rights to read attribute\"" + attributeName + "\" from domain \"" + domainName + "\".");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Creates a new domain with a reduced set of attributes.
     *
     * @param domain the domain to create
     * @return the created domain when the creation was successful, {@code null} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Domain create(Domain domain) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/domain")
                .toUriString();
    	
        // Build and send request
        ResponseEntity<Domain> response = null;
    	try {
        	response = new RestTemplate()
        			.exchange(url, 
        			HttpMethod.POST, 
        			util.createRequestEntity(domain), 
        			Domain.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Creating domain failed: " + e.getMessage());
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		log.debug("The domain that was to be inserted was already in the database.");
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.CREATED) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The parent domain \"" + domain.getSuperDomainName() + "\" was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_ACCEPTABLE) {
    		throw new TrustDeckResponseException("The domain name is violating the URI-validity: \"" + domain.getName() + "\".", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Creating the domain failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Creates a new domain with all attributes.
     *
     * @param domain the domain to create
     * @return the created domain when the creation was successful, {@code null} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Domain createComplete(Domain domain) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/domain/complete")
                .toUriString();
        
        // Build and send request
        ResponseEntity<Domain> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(domain), Domain.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Creating domain failed: " + e.getMessage());
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		log.debug("The domain that was to be inserted was already in the database.");
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.CREATED) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The parent domain \"" + domain.getSuperDomainName() + "\" was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_ACCEPTABLE) {
    		throw new TrustDeckResponseException("The domain name is violating the URI-validity: \"" + domain.getName() + "\".", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Creating the domain failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Updates an existing domain with a reduced set of attributes.
     *
     * @param domainName the name of the domain to update
     * @param domain the updated domain data
     * @return the updated domain when the update was successful, {@code null} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Domain update(String domainName, Domain domain) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/domain")
                .queryParam("name", domainName)
                .toUriString();
        
        // Build and send request
        ResponseEntity<Domain> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(domain), Domain.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Updating domain failed: " + e.getMessage());
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain that is to be updated (" + domainName + ") was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Updating the domain failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Updates an existing domain with all attributes.
     *
     * @param domainName the name of the domain to update
     * @param domain the updated domain data
     * @param recursive whether to apply changes recursively to sub-domains
     * @return the updated domain when the update was successful, {@code null} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Domain updateComplete(String domainName, Domain domain, boolean recursive) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/domain/complete")
                .queryParam("name", domainName)
                .queryParam("recursive", recursive)
                .toUriString();
        
        // Build and send request
        ResponseEntity<Domain> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(domain), Domain.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Updating domain failed: " + e.getMessage());
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		throw new TrustDeckResponseException("The provided salt value was invalid.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain that is to be updated (" + domainName + ") was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_ACCEPTABLE) {
    		throw new TrustDeckResponseException("The new domain name is violating the URI-validity: \"" + domainName + "\".", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Creating the domain failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Deletes a domain.
     *
     * @param domainName the name of the domain to delete
     * @param recursive whether to delete sub-domains recursively
     * @return {@code true} when the deletion was successful, {@code false} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public boolean delete(String domainName, boolean recursive) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/domain")
                .queryParam("name", domainName)
                .queryParam("recursive", recursive)
                .toUriString();
        
        // Build and send request
        ResponseEntity<Void> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Deleting domain failed: " + e.getMessage());
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain that is to be deleted (" + domainName + ") was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
    		log.debug("Deleting the domain failed.");
    		return false;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Updates the salt value of a domain.
     *
     * @param domainName the name of the domain where the salt value should be updated
     * @param newSalt the new salt value
     * @param allowEmpty whether to allow an empty salt value
     * @return the updated domain when the update was successful, {@code null} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Domain updateSalt(String domainName, String newSalt, boolean allowEmpty) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "salt")
                .queryParam("salt", newSalt)
                .queryParam("allowEmpty", allowEmpty)
                .toUriString();
        
        // Build and send request
        ResponseEntity<Domain> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(), Domain.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Updating salt failed: " + e.getMessage());
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		throw new TrustDeckResponseException("The provided salt value was invalid.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain for which the updated salt-value was given (" + domainName + "), couldn't be found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Updating the salt failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }
}