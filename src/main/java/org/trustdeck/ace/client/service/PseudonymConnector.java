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

package org.trustdeck.ace.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.ace.client.config.TrustDeckClientConfig;
import org.trustdeck.ace.client.model.Pseudonym;
import org.trustdeck.ace.client.util.TrustDeckClientUtil;

import java.util.List;

/**
 * A connector library for programmatic interaction with the pseudonym management endpoints
 * of the ACE pseudonymization service in TrustDeck.
 * Provides methods for pseudonym operations (create, read, update, delete).
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Slf4j
@Component
public class PseudonymConnector {

	/** Enables access to the configuration variables. */
	@Autowired
	private TrustDeckClientConfig trustDeckClientConfig;
	
	/** Enables access to utility methods. */
	@Autowired
	private TrustDeckClientUtil util;

	/**
	 * Method to create pseudonyms in a batch.
	 * 
	 * @param domainName the name of the domain where the pseudonyms should be created in
	 * @param omitPrefix a flag deciding whether or not to add the domain-specific prefix to the newly generated pseudonyms
	 * @param pseudonymList the list of pseudonym objects to send to TrustDeck
	 * @return an array of the processed pseudonyms
	 */
    public ResponseEntity<Pseudonym[]> createPseudonymBatch(String domainName, boolean omitPrefix, List<Pseudonym> pseudonymList) {
        try {// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .queryParam("omitPrefix", omitPrefix)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(pseudonymList), Pseudonym[].class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to create pseudonym batch: " + e.getMessage(), e);
        }
    }
    
    /**
     * Method to create a single pseudonym.
     * 
     * @param domainName the name of the domain where the pseudonym should be created in
     * @param pseudonym the pseudonym object to create
     * @param omitPrefix a flag deciding whether or not to add the domain-specific prefix to the newly generated pseudonym
     * @return an array of the created pseudonym object
     */
    public ResponseEntity<Pseudonym[]> createPseudonym(String domainName, Pseudonym pseudonym, boolean omitPrefix) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("omitPrefix", omitPrefix)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(pseudonym), Pseudonym[].class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to create pseudonym: " + e.getMessage(), e);
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
     * @return an array of the linked pseudonyms
     */
    public ResponseEntity<Pseudonym[]> getLinkedPseudonyms(String sourceDomain, String targetDomain, String sourceIdentifier, String sourceIdType, String sourcePsn) {
        try {
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
            return new RestTemplate().exchange(builder.toUriString(), HttpMethod.GET, util.createRequestEntity(), Pseudonym[].class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to get linked pseudonyms: " + e.getMessage(), e);
        }
    }

    /**
     * Method to retrieve a pseudonym with a given identifier and idType.
     * 
     * @param domainName the name of the domain in which the pseudonym should be searched
     * @param identifier the identifier to search for
     * @param idType the type of the identifier
     * @return an array of the found pseudonym object
     */
    public ResponseEntity<Pseudonym[]> getPseudonymByIdentifier(String domainName, String identifier, String idType) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("id", identifier)
                    .queryParam("idType", idType)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Pseudonym[].class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage(), e);
        }
    }

    /**
     * Method to retrieve a pseudonym with a given psn.
     * 
     * @param domainName the name of the domain in which the pseudonym should be searched
     * @param psn the psn to search for
     * @return an array of the found pseudonym object
     */
    public ResponseEntity<Pseudonym[]> getPseudonymByPsn(String domainName, String psn) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("psn", psn)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Pseudonym[].class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage(), e);
        }
    }

    /**
     * A method to retrieve a batch of pseudonyms, aka all pseudonyms in a domain
     * 
     * @param domainName the name of the domain from which the pseudonyms should be retrieved from
     * @return an array with the retrieved pseudonyms
     */
    public ResponseEntity<Pseudonym[]> getPseudonymBatch(String domainName) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Pseudonym[].class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to fetch pseudonym batch: " + e.getMessage(), e);
        }
    }

    /**
     * A method to update a batch of pseudonyms.
     * 
     * @param domainName the name of the domain where the pseudonyms that should be updated are in
     * @param pseudonymList the list of pseudonym objects containing the updated values
     * @return a void entity
     */
    public ResponseEntity<Void> updatePseudonymBatch(String domainName, List<Pseudonym> pseudonymList) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonymList), Void.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to update pseudonym batch: " + e.getMessage(), e);
        }
    }

    /**
     * Method to update all attributes of a pseudonym object that is identified by its identifier.
     * 
     * @param domainName the name of the domain where the pseudonym is stored in
     * @param identifier the identifier used to identify the pseudonym object that should be updated
     * @param idType the type of the identifier
     * @param pseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object
     */
    public ResponseEntity<Pseudonym> updatePseudonymCompleteByIdentifier(String domainName, String identifier, String idType, Pseudonym pseudonym) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
                    .queryParam("id", identifier)
                    .queryParam("idType", idType)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonym), Pseudonym.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to update complete pseudonym: " + e.getMessage(), e);
        }
    }

    /**
     * Method to update all attributes of a pseudonym object that is identified by its pseudonym.
     * 
     * @param domainName the name of the domain where the pseudonym is stored in
     * @param psn the psn-value to identify the pseudonym
     * @param pseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object
     */
    public ResponseEntity<Pseudonym> updatePseudonymCompleteByPsn(String domainName, String psn, Pseudonym pseudonym) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
                    .queryParam("psn", psn)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonym), Pseudonym.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to update complete pseudonym: " + e.getMessage(), e);
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
     * @return the updated pseudonym object
     */
    public ResponseEntity<Pseudonym> updatePseudonymByIdentifier(String domainName, String identifier, String idType, Pseudonym pseudonym) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("id", identifier)
                    .queryParam("idType", idType)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonym), Pseudonym.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage(), e);
        }
    }

    /**
     * Method to update only selected attributes of a pseudonym that is identified by its pseudonym.
     * Updatable attributes are validFrom, validTo, and validityTime.
     * 
     * @param domainName the name of the domain where the pseudonym is stored in
     * @param psn the psn-value to identify the pseudonym
     * @param pseudonym the pseudonym-object containing the updated values
     * @return the updated pseudonym object
     */
    public ResponseEntity<Pseudonym> updatePseudonymByPsn(String domainName, String psn, Pseudonym pseudonym) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("psn", psn)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(pseudonym), Pseudonym.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage(), e);
        }
    }

    /**
     * Method to delete a batch of pseudonyms, aka all pseudonyms in a domain.
     * 
     * @param domainName the name of the domain from which the pseudonyms should be deleted from
     * @return a void entity
     */
    public ResponseEntity<Void> deletePseudonymBatch(String domainName) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to delete pseudonym batch: " + e.getMessage(), e);
        }
    }

    /**
     * Method to delete a pseudonym, identified by either identifier & idType or by psn.
     * 
     * @param domainName the name of the domain from which the pseudonym should be deleted from
     * @param identifier the identifier of the pseudonym object
     * @param idType the type of the identifier
     * @param psn the psn value of the pseudonym object
     * @return a void entity
     */
    public ResponseEntity<Void> deletePseudonym(String domainName, String identifier, String idType, String psn) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym");
            
            if (identifier != null && idType != null) {
                builder.queryParam("id", identifier)
                        .queryParam("idType", idType);
            } else if (psn != null) {
                builder.queryParam("psn", psn);
            } else {
                throw new IllegalArgumentException("Either identifier and idType or psn must be provided.");
            }
            String url = builder.toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to delete pseudonym: " + e.getMessage(), e);
        }
    }

    /**
     * Method to validate the pseudonym using its check-digit.
     * 
     * @param domainName the name of the domain where the pseudonym is stored in
     * @param psn the pseudonym to validate
     * @return the validation result as a String
     */
    public ResponseEntity<String> validatePseudonym(String domainName, String psn) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "validation")
                    .queryParam("psn", psn)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), String.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to validate pseudonym: " + e.getMessage(), e);
        }
    }
}