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
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.util.TrustDeckClientUtil;

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
	private TrustDeckClientUtil util;

	/**
	 * Constructor for a connector handling pseudonym-specific requests.
	 * Initializes the config and the utility object.
	 * 
	 * @param config the configuration for this TrustDeck connection
	 * @param trustDeckClientUtil the helper object handling authentication and some request building tasks 
	 */
	public Pseudonyms(TrustDeckClientConfig config, TrustDeckClientUtil trustDeckClientUtil) {
		this.trustDeckClientConfig = config;
		this.util = trustDeckClientUtil;
	}

	/**
	 * Method to create pseudonyms in a batch.
	 * 
	 * @param domainName the name of the domain where the pseudonyms should be created in
	 * @param pseudonymList the list of pseudonym objects to send to TrustDeck
	 * @param omitPrefix a flag deciding whether or not to add the domain-specific prefix to the newly generated pseudonyms
	 * @return a list of the processed pseudonyms, or {@code null} when the request was unsuccessful
	 */
    public List<Pseudonym> createPseudonymBatch(String domainName, List<Pseudonym> pseudonymList, boolean omitPrefix) {
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
            log.error("Creating pseudonyms in a batch failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.CREATED) {
    		return List.of(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain \"" + domainName + "\" was not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Batch insertion of pseudonyms failed.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
    		log.debug("Pseudonymization of an identifier failed. Batch insertion was aborted.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.INSUFFICIENT_STORAGE) {
    		log.debug("The domain does not provide enough pseudonyms for the request.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }
    
    /**
     * Method to create a single pseudonym.
     * 
     * @param domainName the name of the domain where the pseudonym should be created in
     * @param pseudonym the pseudonym object to create
     * @param omitPrefix a flag deciding whether or not to add the domain-specific prefix to the newly generated pseudonym
     * @return the created pseudonym object, or {@code null} when the request was unsuccessful
     */
    public Pseudonym createPseudonym(String domainName, Pseudonym pseudonym, boolean omitPrefix) {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("omitPrefix", omitPrefix)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym[]> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(pseudonym), Pseudonym[].class);
        } catch (RestClientException e) {
            log.error("Creating pseudonym failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		log.debug("Insertion of the pseudonym was skipped because it is already in the database.");
    		return response.getBody()[0];
    	} else if (response.getStatusCode() == HttpStatus.CREATED) {
    		return response.getBody()[0];
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain \"" + domainName + "\" was not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Insertion of pseudonym failed.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
    		log.debug("Pseudonymization of an identifier failed.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.INSUFFICIENT_STORAGE) {
    		log.debug("The domain does not provide enough pseudonyms for the request.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * A method to search and link pseudonyms along the pseudonym-chain in the tree.
     * 
     * @param sourceDomain the starting domain for the search
     * @param targetDomain the target domain for the search
     * @param sourceIdentifier the identifier of the record to start the search from
     * @param sourceIdType the idType of the record to start the search from
     * @param sourcePsn the pseudonym of the record to start the search from
     * @return a list of the linked pseudonym pairs (pairs are represented as lists), or {@code null} when the request was unsuccessful
     */
    public List<List<Pseudonym>> getLinkedPseudonyms(String sourceDomain, String targetDomain, String sourceIdentifier, String sourceIdType, String sourcePsn) {
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
            log.error("Retrieving linked pseudonyms failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
    		log.debug("The requesting user did not have all the required rights to perform this request.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("No linkable pseudonyms were found.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * Method to retrieve a pseudonym with a given identifier and idType.
     * 
     * @param domainName the name of the domain in which the pseudonym should be searched
     * @param identifier the identifier to search for
     * @param idType the type of the identifier
     * @return the found pseudonym object, or {@code null} when the request was unsuccessful
     */
    public Pseudonym getPseudonymByIdentifier(String domainName, String identifier, String idType) {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("id", identifier)
                .queryParam("idType", idType)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym[]> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Pseudonym[].class);
        } catch (RestClientException e) {
            log.error("Retrieving pseudonym failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody()[0];
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("No pseudonym was found.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * Method to retrieve a pseudonym with a given psn.
     * 
     * @param domainName the name of the domain in which the pseudonym should be searched
     * @param psn the psn to search for
     * @return an array of the found pseudonym object, or {@code null} when the request was unsuccessful
     */
    public Pseudonym getPseudonymByPsn(String domainName, String psn) {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("psn", psn)
                    .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym[]> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Pseudonym[].class);
        } catch (RestClientException e) {
            log.error("Retrieving pseudonym failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody()[0];
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("No pseudonym was found.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * A method to retrieve a batch of pseudonyms, aka all pseudonyms in a domain
     * 
     * @param domainName the name of the domain from which the pseudonyms should be retrieved from
     * @return an array with the retrieved pseudonyms, or {@code null} when the request was unsuccessful
     */
    public List<Pseudonym> getPseudonymBatch(String domainName) {
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
            log.error("Retrieving pseudonyms failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return List.of(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("Domain \"" + domainName + "\" was not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Pseudonym retrieval failed.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * A method to update a batch of pseudonyms.
     * 
     * @param domainName the name of the domain where the pseudonyms that should be updated are in
     * @param pseudonymList the list of pseudonym objects containing the updated values
     * @return {@code true} when the update was successful, {@code false} otherwise
     */
    public boolean updatePseudonymBatch(String domainName, List<Pseudonym> pseudonymList) {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Void> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonymList), Void.class);
        } catch (RestClientException e) {
            log.error("Updating pseudonyms failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("Domain \"" + domainName + "\" was not found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Pseudonym batch update failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }

    /**
     * Method to update all attributes of a pseudonym object that is identified by its identifier.
     * 
     * @param domainName the name of the domain where the pseudonym is stored in
     * @param identifier the identifier used to identify the pseudonym object that should be updated
     * @param idType the type of the identifier
     * @param pseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object, or {@code null} when unsuccessful
     */
    public Pseudonym updatePseudonymCompleteByIdentifier(String domainName, String identifier, String idType, Pseudonym pseudonym) {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
                .queryParam("id", identifier)
                .queryParam("idType", idType)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonym), Pseudonym.class);
        } catch (RestClientException e) {
            log.error("Updating pseudonym failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
    		log.debug("The user requested to change the domain of a pseudonym-record to a domain the user has no rights for.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain or the pseudonym that is to be updated were not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Update of pseudonym failed.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * Method to update all attributes of a pseudonym object that is identified by its pseudonym.
     * 
     * @param domainName the name of the domain where the pseudonym is stored in
     * @param psn the psn-value to identify the pseudonym
     * @param pseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object, or {@code null} when unsuccessful
     */
    public Pseudonym updatePseudonymCompleteByPsn(String domainName, String psn, Pseudonym pseudonym) {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
        		.queryParam("psn", psn)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonym), Pseudonym.class);
        } catch (RestClientException e) {
            log.error("Updating pseudonym failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
    		log.debug("The user requested to change the domain of a pseudonym-record to a domain the user has no rights for.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain or the pseudonym that is to be updated were not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Update of pseudonym failed.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * Method to update only selected attributes of a pseudonym that is identified by its identifier.
     * Updatable attributes are validFrom, validTo, and validityTime.
     * 
     * @param domainName the name of the domain where the pseudonym is stored in
     * @param identifier the identifier used to identify the pseudonym object that should be updated
     * @param idType the type of the identifier
     * @param pseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object, or {@code null} when unsuccessful
     */
    public Pseudonym updatePseudonymByIdentifier(String domainName, String identifier, String idType, Pseudonym pseudonym) {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("id", identifier)
                .queryParam("idType", idType)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonym), Pseudonym.class);
        } catch (RestClientException e) {
            log.error("Updating pseudonym failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain or the pseudonym that is to be updated were not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Update of pseudonym failed.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * Method to update only selected attributes of a pseudonym that is identified by its pseudonym.
     * Updatable attributes are validFrom, validTo, and validityTime.
     * 
     * @param domainName the name of the domain where the pseudonym is stored in
     * @param psn the psn-value to identify the pseudonym
     * @param pseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object, or {@code null} when unsuccessful
     */
    public Pseudonym updatePseudonymByPsn(String domainName, String psn, Pseudonym pseudonym) {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                .queryParam("psn", psn)
                .toUriString();
        
        // Build and send request
    	ResponseEntity<Pseudonym> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonym), Pseudonym.class);
        } catch (RestClientException e) {
            log.error("Updating pseudonym failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain or the pseudonym that is to be updated were not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Update of pseudonym failed.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * Method to delete a batch of pseudonyms, aka all pseudonyms in a domain.
     * 
     * @param domainName the name of the domain from which the pseudonyms should be deleted from
     * @return {@code true} when the deletion was successful, {@code false} otherwise
     */
    public boolean deletePseudonymBatch(String domainName) {
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
            log.error("Deleting pseudonyms failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("Domain \"" + domainName + "\" was not found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Pseudonym batch deletion failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }

    /**
     * Method to delete a pseudonym, identified by either identifier & idType or by psn.
     * 
     * @param domainName the name of the domain from which the pseudonym should be deleted from
     * @param identifier the identifier of the pseudonym object
     * @param idType the type of the identifier
     * @param psn the psn value of the pseudonym object
     * @return {@code true} when the deletion was successful, {@code false} otherwise
     */
    public boolean deletePseudonym(String domainName, String identifier, String idType, String psn) {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym");
        
        if (identifier != null && idType != null && psn != null) {
            builder.queryParam("id", identifier)
                   .queryParam("idType", idType)
            	   .queryParam("psn", psn);
        } else if (identifier != null && idType != null) {
            builder.queryParam("id", identifier)
            	   .queryParam("idType", idType);
        } else if (psn != null) {
            builder.queryParam("psn", psn);
        } else {
            throw new IllegalArgumentException("Either identifier and idType, or psn, or all three must be provided.");
        }
        String url = builder.toUriString();
        
        // Build and send request
    	ResponseEntity<Void> response = null;
    	try {
        	response = new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            log.error("Deleting pseudonym failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		log.debug("Invalid configuration of parameters. At least an id and idType or the psn is needed. Ideally all three.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("Domain \"" + domainName + "\" was not found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Pseudonym deletion failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }

    /**
     * Method to validate the pseudonym using its check-digit.
     * 
     * @param domainName the name of the domain where the pseudonym is stored in
     * @param psn the pseudonym to validate
     * @return {@code true} if the pseudonym is valid, {@code false} otherwise
     */
    public boolean validatePseudonym(String domainName, String psn) {
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
            log.error("Validating pseudonym failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return Boolean.valueOf(response.getBody());
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		log.debug("The validation was aborted since a character that is not part of the allowed alphabet was encountered.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("Domain \"" + domainName + "\" was not found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Validation failed since the domain was configured to have no check digit.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }
}