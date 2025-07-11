package org.trustdeck.ace.client.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.ace.client.model.Domain;
import org.trustdeck.ace.client.util.AceClientUtil;

/**
 * A connector library for programmatic interaction with the domain management endpoints
 * of the ACE pseudonymization service.
 * Provides methods for domain operations (create, retrieve, update, delete)
 * and handles Keycloak authentication using the password grant type.
 */
@Slf4j
public class DomainConnector {

    private final String serviceUrl;
    private final RestTemplate restTemplate;
    private final Keycloak keycloakClient;
    private final AceClientUtil aceClientUtil;

    /**
     * Constructor to initialize the connector with service URL and authentication configuration.
     *
     * @param serviceUrl    The URI to the ACE instance
     * @param keycloakUrl   The Keycloak server URL
     * @param realm         The Keycloak realm
     * @param clientId      The Keycloak client ID
     * @param clientSecret  The Keycloak client secret
     * @param userName      The Keycloak username
     * @param password      The Keycloak password
     */
    public DomainConnector(String serviceUrl, String keycloakUrl, String realm, String clientId, String clientSecret, String userName, String password) {
        this.serviceUrl = serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/";
        this.restTemplate = new RestTemplate();
        this.aceClientUtil = new AceClientUtil();

        try {
            this.keycloakClient = KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(userName)
                    .password(password)
                    .grantType("password")
                    .build();
            log.info("Successfully initialized Keycloak client at {} under realm: {} and client: {}", keycloakUrl, realm, clientId);
            aceClientUtil.ensureValidTokenOrRefresh(keycloakClient);
        } catch (Exception e) {
            throw new RuntimeException("Keycloak client could not be initialized", e);
        }
    }

    /**
     * Gets a list of all domains.
     *
     * @return List of Domain objects
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<Domain[]> getAllDomains() {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/experimental/domains/hierarchy")
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Domain[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Domain[].class
            );
            return response;
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
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain")
                    .queryParam("name", domainName)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Domain> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Domain.class
            );
            return response;
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
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, attributeName)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Domain> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Domain.class
            );
            log.info("Response status: {} body: {}", response.getStatusCode().value(),
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
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain")
                    .toUriString();
            HttpEntity<Domain> request = new HttpEntity<>(domain, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Domain> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Domain.class
            );
            return response;
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
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain/complete")
                    .toUriString();
            HttpEntity<Domain> request = new HttpEntity<>(domain, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Domain> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Domain.class
            );
            return response;
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
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain")
                    .queryParam("name", domainName)
                    .toUriString();
            HttpEntity<Domain> request = new HttpEntity<>(domain, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Domain> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    Domain.class
            );
            return response;
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
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain/complete")
                    .queryParam("name", domainName)
                    .queryParam("recursive", recursive)
                    .toUriString();
            HttpEntity<Domain> request = new HttpEntity<>(domain, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Domain> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    Domain.class
            );
            return response;
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
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain")
                    .queryParam("name", domainName)
                    .queryParam("recursive", recursive)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            return restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );
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
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "salt")
                    .queryParam("salt", newSalt)
                    .queryParam("allowEmpty", allowEmpty)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Domain> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    Domain.class
            );
            return response;
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update salt: " + e.getMessage(), e);
        }
    }
}