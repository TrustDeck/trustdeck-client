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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.util.TrustDeckClientUtil;

/**
 * A connector library for programmatic interaction with the domain management endpoints
 * of the ACE pseudonymization service in TrustDeck.
 * Provides methods for domain operations (create, read, update, delete).
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Slf4j
@Component
public class DomainConnector {

	/** Enables access to the configuration variables. */
	@Autowired
	private TrustDeckClientConfig trustDeckClientConfig;
	
	/** Enables access to utility methods. */
	@Autowired
	private TrustDeckClientUtil util;

    /**
     * Gets a list of all domains.
     *
     * @return List of Domain objects
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<Domain[]> getAllDomains() {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                    .path("api/pseudonymization/experimental/domains/hierarchy")
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Domain[].class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to retrieve domains: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a domain by name.
     *
     * @param domainName The name of the domain
     * @return The requested Domain
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<Domain> getDomain(String domainName) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                    .path("api/pseudonymization/domain")
                    .queryParam("name", domainName)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Domain.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to retrieve domain: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a specific attribute of a domain.
     *
     * @param domainName    The name of the domain
     * @param attributeName The name of the attribute to retrieve
     * @return The requested Domain containing only the specified attribute
     * @throws RuntimeException if the request fails or authentication fails
     */
    public  ResponseEntity<Domain> getDomainAttribute(String domainName, String attributeName) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                    .pathSegment("api", "pseudonymization", "domains", domainName, attributeName)
                    .toUriString();
         
            // Build and send request
            ResponseEntity<Domain> response =  new RestTemplate().exchange(url, HttpMethod.GET, util.createRequestEntity(), Domain.class);
            
            log.debug("Response status: {}, body: {}", response.getStatusCode().value(),
                    HttpStatus.valueOf(response.getStatusCode().value()).getReasonPhrase());
            
            return response;
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to retrieve domain attribute: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new domain with a reduced set of attributes.
     *
     * @param domain The domain to create
     * @return The created Domain
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<Domain> createDomain(Domain domain) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                    .path("api/pseudonymization/domain")
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(domain), Domain.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to create domain: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new domain with all attributes.
     *
     * @param domain The domain to create
     * @return The created Domain
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<Domain> createDomainComplete(Domain domain) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                    .path("api/pseudonymization/domain/complete")
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.POST, util.createRequestEntity(domain), Domain.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to create domain: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing domain with a reduced set of attributes.
     *
     * @param domainName The name of the domain to update
     * @param domain     The updated domain data
     * @return The updated Domain
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<Domain> updateDomain(String domainName, Domain domain) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                    .path("api/pseudonymization/domain")
                    .queryParam("name", domainName)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(domain), Domain.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to update domain: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing domain with all attributes.
     *
     * @param domainName The name of the domain to update
     * @param domain     The updated domain data
     * @param recursive  Whether to apply changes recursively to sub-domains
     * @return The updated Domain
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<Domain> updateDomainComplete(String domainName, Domain domain, boolean recursive) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                    .path("api/pseudonymization/domain/complete")
                    .queryParam("name", domainName)
                    .queryParam("recursive", recursive)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(domain), Domain.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to update domain: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a domain.
     *
     * @param domainName The name of the domain to delete
     * @param recursive  Whether to delete sub-domains recursively
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<Void> deleteDomain(String domainName, boolean recursive) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
                    .path("api/pseudonymization/domain")
                    .queryParam("name", domainName)
                    .queryParam("recursive", recursive)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.DELETE, util.createRequestEntity(), Void.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to delete domain: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the salt of a domain.
     *
     * @param domainName The name of the domain
     * @param newSalt    The new salt value
     * @param allowEmpty Whether to allow an empty salt
     * @return The updated Domain
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<Domain> updateSalt(String domainName, String newSalt, boolean allowEmpty) {
        try {
        	// Build request URL
        	String serviceUrl = trustDeckClientConfig.getServiceUrl();
            String url = UriComponentsBuilder.fromUriString(serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/")
            		.pathSegment("api", "pseudonymization", "domains", domainName, "salt")
                    .queryParam("salt", newSalt)
                    .queryParam("allowEmpty", allowEmpty)
                    .toUriString();
            
            // Build and send request
            return new RestTemplate().exchange(url, HttpMethod.PUT, util.createRequestEntity(), Domain.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to update salt: " + e.getMessage(), e);
        }
    }
}