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

import org.springframework.core.ParameterizedTypeReference;
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
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.util.TrustDeckRequestUtil;

import java.util.Arrays;
import java.util.List;

/**
 * A connector library for programmatic interaction with the pseudonym management endpoints
 * of the ACE pseudonymization service in TrustDeck.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Slf4j
public class Pseudonyms {

	/** Enables access to the configuration variables. */
	private TrustDeckClientConfig trustDeckClientConfig;
	
	/** Enables access to utility methods. */
	private TrustDeckRequestUtil util;
	
	/** The name of the domain where the desired pseudonym-interaction is performed in. */
	private String domainName;

	/**
	 * Constructor for a connector handling pseudonym-specific requests.
	 * Initializes the config and the utility object.
	 * 
	 * @param config the configuration for this TrustDeck connection
	 * @param trustDeckRequestUtil the helper object handling authentication and some request building tasks 
	 * @param domainName the name of the domain where the desired pseudonym-interactions are performed in
	 */
	public Pseudonyms(TrustDeckClientConfig config, TrustDeckRequestUtil trustDeckRequestUtil, String domainName) {
		this.trustDeckClientConfig = config;
		this.util = trustDeckRequestUtil;
		this.domainName = domainName;
	}

	/**
	 * Method to create pseudonyms in a batch.
	 * 
	 * @param pseudonymList the list of pseudonym objects to send to TrustDeck
	 * @param omitPrefix a flag deciding whether or not to add the domain-specific prefix to the newly generated pseudonyms
	 * @return a list of the processed pseudonyms, or {@code null} when the request was unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
	 */
    public List<Pseudonym> createBatch(List<Pseudonym> pseudonymList, boolean omitPrefix) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                .queryParam("omitPrefix", omitPrefix)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym[]> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(pseudonymList), Pseudonym[].class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Creating a batch of pseudonyms failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.CREATED) {
    		return Arrays.asList(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain \"" + domainName + "\" was not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		throw new TrustDeckResponseException("Batch insertion of pseudonyms failed.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
    		throw new TrustDeckResponseException("Pseudonymization of an identifier failed. Batch insertion was aborted.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.INSUFFICIENT_STORAGE) {
    		throw new TrustDeckResponseException("The domain does not provide enough pseudonyms for the request.", response.getStatusCode());
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }
    
    /**
     * Method to create a single pseudonym.
     * 
     * @param pseudonym the pseudonym object to create
     * @param omitPrefix a flag deciding whether or not to add the domain-specific prefix to the newly generated pseudonym
     * @return the created pseudonym object, or {@code null} when the request was unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Pseudonym create(Pseudonym pseudonym, boolean omitPrefix) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("omitPrefix", omitPrefix)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(pseudonym), Pseudonym.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Creating pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		log.debug("Insertion of the pseudonym was skipped because it is already in the database.");
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.CREATED) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain \"" + domainName + "\" was not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		throw new TrustDeckResponseException("Insertion of pseudonym failed.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
    		throw new TrustDeckResponseException("Pseudonymization of an identifier failed.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.INSUFFICIENT_STORAGE) {
    		throw new TrustDeckResponseException("The domain does not provide enough pseudonyms for the request.", response.getStatusCode());
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }
    
    /**
     * Method to create a single pseudonym by only providing an identifier 
     * and an idType (encapsulated as an IdentifierItem).
     * 
     * @param identifierItem the identifier item containing the actual identifier and the idType
     * @param omitPrefix a flag deciding whether or not to add the domain-specific prefix to the newly generated pseudonym
     * @return the created pseudonym object, or {@code null} when the request was unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Pseudonym create(IdentifierItem identifierItem, boolean omitPrefix) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        return create(Pseudonym.builder().identifierItem(identifierItem).build(), omitPrefix);
    }

    /**
     * A method to search and link pseudonyms along the pseudonym-chain in the tree.
     * 
     * @param sourceDomain the starting domain for the search
     * @param targetDomain the target domain for the search
     * @param sourceIdentifier the identifier of the record to start the search from
     * @param sourceIdType the idType of the record to start the search from
     * @param sourcePsn the pseudonym of the record to start the search from
     * @return a list of the linked pseudonym pairs (pairs are represented as lists), 
     * 			or {@code null} when the request was unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public List<List<Pseudonym>> getLinkedPseudonyms(String sourceDomain, String targetDomain, String sourceIdentifier, String sourceIdType, String sourcePsn) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
    	UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
	            .path("api/pseudonymization/domains/linked-pseudonyms")
	            .queryParam("sourceDomain", sourceDomain)
	            .queryParam("targetDomain", targetDomain);
	    if (sourceIdentifier != null) builder.queryParam("sourceIdentifier", sourceIdentifier);
	    if (sourceIdType != null) builder.queryParam("sourceIdType", sourceIdType);
	    if (sourcePsn != null) builder.queryParam("sourcePsn", sourcePsn);
        
        // Build and send request
    	ResponseEntity<List<List<Pseudonym>>> response = null;
    	try {
    		response = new RestTemplate().exchange(builder.toUriString(), HttpMethod.GET, util.createRequestEntity(), new ParameterizedTypeReference<List<List<Pseudonym>>>() {});
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Retrieving linked pseudonyms failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
    		throw new TrustDeckResponseException("The requesting user did not have all the required rights to perform this request.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("No linkable pseudonyms were found.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to retrieve a pseudonym with a given identifier and idType (encapsulated as an identifier item).
     * 
     * @param identifierItem the identifier and its type (encapsulated as an identifier item) to search for
     * @return the found pseudonym object, or {@code null} when the request was unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Pseudonym get(IdentifierItem identifierItem) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("id", identifierItem.getIdentifier())
                .queryParam("idType", identifierItem.getIdType())
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Pseudonym.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Retrieving pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("No pseudonym was found.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to retrieve a pseudonym with a given psn.
     * 
     * @param psn the psn to search for
     * @return the found pseudonym object, or {@code null} when the request was unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Pseudonym get(String psn) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("psn", psn)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Pseudonym.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Retrieving pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("No pseudonym was found.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * A method to retrieve a batch of pseudonyms, aka all pseudonyms in a domain
     * 
     * @return a list with the retrieved pseudonyms, or {@code null} when the request was unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public List<Pseudonym> getBatch() throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym[]> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Pseudonym[].class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Retrieving pseudonyms failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return Arrays.asList(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("Domain \"" + domainName + "\" was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Pseudonym retrieval failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * A method to update a batch of pseudonyms.
     * 
     * @param pseudonymList the list of pseudonym objects containing the updated values
     * @return a list of the updated pseudonyms when the update was successful, {@code null} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public List<Pseudonym> updateBatch(List<Pseudonym> pseudonymList) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                .toUriString();
        
        // Build and send request
        ResponseEntity<Pseudonym[]> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonymList), Pseudonym[].class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Updating pseudonyms failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return Arrays.asList(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("Domain \"" + domainName + "\" was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Pseudonym batch update failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to update all attributes of a pseudonym object that is identified by its identifier.
     * 
     * @param identifierItem the identifier and its Type (encapsulated as an identifier item)
     * 			used to identify the pseudonym object that should be updated
     * @param updatePseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object, or {@code null} when unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Pseudonym updateComplete(IdentifierItem identifierItem, Pseudonym updatePseudonym) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
                .queryParam("id", identifierItem.getIdentifier())
                .queryParam("idType", identifierItem.getIdType())
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(updatePseudonym), Pseudonym.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Updating pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
    		throw new TrustDeckResponseException("The user requested to change the domain of a pseudonym-record to a domain the user has no rights for.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain or the pseudonym that is to be updated were not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Update of pseudonym failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to update all attributes of a pseudonym object that is identified by its psn.
     * 
     * @param psn the psn-value to identify the pseudonym
     * @param updatePseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object, or {@code null} when unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Pseudonym updateComplete(String psn, Pseudonym updatePseudonym) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
        		.queryParam("psn", psn)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(updatePseudonym), Pseudonym.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Updating pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
    		throw new TrustDeckResponseException("The user requested to change the domain of a pseudonym-record to a domain the user has no rights for.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain or the pseudonym that is to be updated were not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Update of pseudonym failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to update only selected attributes of a pseudonym that is identified by its identifier.
     * Updatable attributes are validFrom, validTo, and validityTime.
     * 
     * @param identifierItem the identifier and its type (encapsulated as an identifier item) 
     * 			used to identify the pseudonym object that should be updated
     * @param updatePseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object, or {@code null} when unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Pseudonym update(IdentifierItem identifierItem, Pseudonym updatePseudonym) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("id", identifierItem.getIdentifier())
                .queryParam("idType", identifierItem.getIdType())
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(updatePseudonym), Pseudonym.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Updating pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain or the pseudonym that is to be updated were not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Update of pseudonym failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to update only selected attributes of a pseudonym that is identified by its psn.
     * Updatable attributes are validFrom, validTo, and validityTime.
     * 
     * @param psn the psn-value to identify the pseudonym
     * @param updatePseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object, or {@code null} when unsuccessful
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public Pseudonym update(String psn, Pseudonym updatePseudonym) throws TrustDeckClientLibraryException, TrustDeckResponseException {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("psn", psn)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(updatePseudonym), Pseudonym.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Updating pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("The domain or the pseudonym that is to be updated were not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Update of pseudonym failed.");
    		return null;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to delete a batch of pseudonyms, aka all pseudonyms in a domain.
     * 
     * @return {@code true} when the deletion was successful, {@code false} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public boolean deleteBatch() throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Void> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Deleting pseudonyms failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("Domain \"" + domainName + "\" was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Pseudonym batch deletion failed.");
    		return false;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to delete a pseudonym, identified by identifier & idType (encapsulated as an identifier item).
     * 
     * @param identifierItem the identifier and its type
     * @return {@code true} when the deletion was successful, {@code false} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public boolean delete(IdentifierItem identifierItem) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
        		.queryParam("id", identifierItem.getIdentifier())
        		.queryParam("idType", identifierItem.getIdType())
        		.toUriString();
        
        // Build and send request
    	ResponseEntity<Void> response = null;
    	try {
        	response = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Deleting pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		throw new TrustDeckResponseException("Invalid configuration of parameters. At least an id and idType or the psn is needed.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("Domain \"" + domainName + "\" was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Pseudonym deletion failed.");
    		return false;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to delete a pseudonym, identified its psn.
     * 
     * @param psn the psn value of the pseudonym object
     * @return {@code true} when the deletion was successful, {@code false} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public boolean delete(String psn) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
        		.queryParam("psn", psn)
        		.toUriString();
        
        // Build and send request
    	ResponseEntity<Void> response = null;
    	try {
        	response = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Deleting pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		throw new TrustDeckResponseException("Invalid configuration of parameters. At least an id and idType or the psn is needed.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("Domain \"" + domainName + "\" was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Pseudonym deletion failed.");
    		return false;
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }

    /**
     * Method to validate the pseudonym using its check-digit.
     * 
     * @param psn the pseudonym to validate
     * @return {@code true} if the pseudonym is valid, {@code false} otherwise
     * @throws TrustDeckClientLibraryException when sending the request to TrustDeck failed
     * @throws TrustDeckResponseException when the response from TrustDeck is not as expected
     */
    public boolean validate(String psn) throws TrustDeckClientLibraryException, TrustDeckResponseException {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "validation")
                .queryParam("psn", psn)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<String> response = null;
    	try {
        	response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), String.class);
        } catch (RestClientException e) {
            // Wrap the exception and re-throw
            throw new TrustDeckClientLibraryException("Validating pseudonym failed: " + e.getMessage(), e);
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return Boolean.valueOf(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		log.debug("A character that is not part of the allowed alphabet was encountered.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		throw new TrustDeckResponseException("Domain \"" + domainName + "\" was not found.", response.getStatusCode());
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		throw new TrustDeckResponseException("Validation failed since the domain was configured to have no check digit.", response.getStatusCode());
    	} else {
    		throw new TrustDeckResponseException("Unexpected status code in response.", response.getStatusCode());
    	}
    }
}