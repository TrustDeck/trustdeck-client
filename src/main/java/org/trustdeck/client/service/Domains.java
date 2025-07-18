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
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.util.TrustDeckClientUtil;

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
	private TrustDeckClientUtil util;

	/**
	 * Constructor for a connector handling domain-specific requests.
	 * Initializes the config and the utility object.
	 * 
	 * @param config the configuration for this TrustDeck connection
	 * @param trustDeckClientUtil the helper object handling authentication and some request building tasks 
	 */
	public Domains(TrustDeckClientConfig config, TrustDeckClientUtil trustDeckClientUtil) {
		this.trustDeckClientConfig = config;
		this.util = trustDeckClientUtil;
	}
	
    /**
     * Gets a list of all domains.
     *
     * @return list of domain objects or {@code null} when unsuccessful
     */
    public List<Domain> getAllDomains() {
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
            log.error("Retrieving all domains failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return Arrays.asList(response.getBody());
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * Gets a domain by name.
     *
     * @param domainName the name of the domain
     * @return the requested domain or {@code null} when unsuccessful
     */
    public Domain getDomain(String domainName) {
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
            log.error("Retrieving domain failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return response.getBody();
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain \"" + domainName + "\" was not found.");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * Gets a specific attribute of a domain.
     *
     * @param domainName the name of the domain
     * @param attributeName the name of the attribute to retrieve
     * @return the requested domain attribute as a String or {@code null} when unsuccessful
     */
    public String getDomainAttribute(String domainName, String attributeName) {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .pathSegment("api", "pseudonymization", "domains", domainName, attributeName)
                .toUriString();
    	
        // Build and send request
        ResponseEntity<Domain> response = null;
    	try {
            response = new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Domain.class);
        } catch (RestClientException e) {
            log.error("Retrieving domain attribute failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return null;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return util.extractAttributeOutOfDomain(response.getBody(), attributeName);
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain \"" + domainName + "\" was not found.");
    		return null;
    	} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
    		log.debug("Insufficient rights to read attribute\"" + attributeName + "\" from domain \"" + domainName + "\".");
    		return null;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return null;
    	}
    }

    /**
     * Creates a new domain with a reduced set of attributes.
     *
     * @param domain the domain to create
     * @return {@code true} when the creation was successful, {@code false} otherwise
     */
    public boolean createDomain(Domain domain) {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/domain")
                .toUriString();
    	
        // Build and send request
        ResponseEntity<Domain> response = null;
    	try {
        	response = new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(domain), Domain.class);
        } catch (RestClientException e) {
            log.error("Creating domain failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		log.debug("The domain that was to be inserted was already in the database.");
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.CREATED) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The parent domain \"" + domain.getSuperDomainName() + "\" was not found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.NOT_ACCEPTABLE) {
    		log.debug("The domain name is violating the URI-validity: \"" + domain.getName() + "\".");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Creating the domain failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }

    /**
     * Creates a new domain with all attributes.
     *
     * @param domain the domain to create
     * @return {@code true} when the creation was successful, {@code false} otherwise
     */
    public boolean createDomainComplete(Domain domain) {
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
            log.error("Creating domain failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		log.debug("The domain that was to be inserted was already in the database.");
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.CREATED) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The parent domain \"" + domain.getSuperDomainName() + "\" was not found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.NOT_ACCEPTABLE) {
    		log.debug("The domain name is violating the URI-validity: \"" + domain.getName() + "\".");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Creating the domain failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }

    /**
     * Updates an existing domain with a reduced set of attributes.
     *
     * @param domainName the name of the domain to update
     * @param domain the updated domain data
     * @return {@code true} when the update was successful, {@code false} otherwise
     */
    public boolean updateDomain(String domainName, Domain domain) {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/domain")
                .queryParam("name", domainName)
                .toUriString();
        
        // Build and send request
        ResponseEntity<String> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(domain), String.class);
        } catch (RestClientException e) {
            log.error("Updating domain failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain that is to be updated (" + domainName + ") was not found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Updating the domain failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }

    /**
     * Updates an existing domain with all attributes.
     *
     * @param domainName the name of the domain to update
     * @param domain the updated domain data
     * @param recursive whether to apply changes recursively to sub-domains
     * @return {@code true} when the update was successful, {@code false} otherwise
     */
    public boolean updateDomainComplete(String domainName, Domain domain, boolean recursive) {
        // Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                .path("api/pseudonymization/domain/complete")
                .queryParam("name", domainName)
                .queryParam("recursive", recursive)
                .toUriString();
        
        // Build and send request
        ResponseEntity<String> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(domain), String.class);
        } catch (RestClientException e) {
            log.error("Updating domain failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		log.debug("The provided salt value was invalid.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain that is to be updated (" + domainName + ") was not found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.NOT_ACCEPTABLE) {
    		log.debug("The new domain name is violating the URI-validity: \"" + domainName + "\".");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Creating the domain failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }

    /**
     * Deletes a domain.
     *
     * @param domainName the name of the domain to delete
     * @param recursive whether to delete sub-domains recursively
     * @return {@code true} when the deletion was successful, {@code false} otherwise
     */
    public boolean deleteDomain(String domainName, boolean recursive) {
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
            log.error("Deleting domain failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain that is to be deleted (" + domainName + ") was not found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
    		log.debug("Deleting the domain failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }

    /**
     * Updates the salt of a domain.
     *
     * @param domainName the name of the domain
     * @param newSalt the new salt value
     * @param allowEmpty whether to allow an empty salt
     * @return {@code true} when the update was successful, {@code false} otherwise
     */
    public boolean updateSalt(String domainName, String newSalt, boolean allowEmpty) {
    	// Build request URL
    	String serviceUrl = trustDeckClientConfig.getServiceUrl();
        String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
        		.pathSegment("api", "pseudonymization", "domains", domainName, "salt")
                .queryParam("salt", newSalt)
                .queryParam("allowEmpty", allowEmpty)
                .toUriString();
        
        // Build and send request
        ResponseEntity<Void> response = null;
    	try {
    		response = new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(), Void.class);
        } catch (RestClientException e) {
            log.error("Updating salt failed: " + e.getMessage());
            if (log.isTraceEnabled()) {
            	e.printStackTrace();
            }
            
            return false;
        }
    	
    	// Check response
    	if (response.getStatusCode() == HttpStatus.OK) {
    		return true;
    	} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
    		log.debug("The provided salt value was invalid.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
    		log.debug("The domain for which the updated salt-value was given (" + domainName + "), couldn't be found.");
    		return false;
    	} else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
    		log.debug("Updating the salt failed.");
    		return false;
    	} else {
    		log.debug("Unexpected status code in response: " + response.getStatusCode());
    		return false;
    	}
    }
}