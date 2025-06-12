package org.trustdeck.ace.client.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.ace.client.dto.DomainDto;
import java.util.List;
import org.trustdeck.ace.client.util.AceClientUtil;
/**
 * A connector library for programmatic interaction with the domain management endpoints
 * of the ACE pseudonymization service.
 * Provides methods for domain operations (create, retrieve, update, delete)
 * and handles Keycloak authentication using the password grant type.
 */
@Slf4j
public class DomainConnector {

    private final String serviceUrl; // Base URL of the pseudonymization service
    private final RestTemplate restTemplate; // HTTP client for REST API calls
    private final Keycloak keycloakClient; // Keycloak client for authentication
    private final AceClientUtil aceClientUtil; //  HTTP header creation (with authentication and content type) along with token management.

    /**
     * Constructor to initialize the connector with user-provided configuration.
     *
     * @param serviceUrl   URI to the ACE instance
     * @param keycloakUrl  URI to the Keycloak instance
     * @param realm        Keycloak realm name
     * @param clientId     Keycloak client ID
     * @param clientSecret Keycloak client secret
     * @param username     Keycloak username
     * @param password     Keycloak user password
     */
    public DomainConnector(String serviceUrl, String keycloakUrl, String realm,
                           String clientId, String clientSecret, String username, String password) {
        this.serviceUrl = serviceUrl.endsWith("/") ? serviceUrl : serviceUrl + "/";
        this.restTemplate = new RestTemplate();
        this.aceClientUtil = new AceClientUtil();

        try {
            this.keycloakClient = KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(username)
                    .password(password)
                    .grantType("password")
                    .build();
            log.info("Successfully initialized Keycloak client at {} under realm: {} and client: {}", keycloakUrl, realm, clientId);
            aceClientUtil.ensureValidTokenOrRefresh(keycloakClient); // Initial token fetch
        } catch (Exception e) {
            throw new RuntimeException("Keycloak client could not be initialized", e);
        }
    }

    /**
     * Gets a list of all domains.
     *
     * @return List of DomainDto objects
     * @throws RuntimeException if the request fails or authentication fails
     */
    public ResponseEntity<DomainDto[]> getAllDomains() {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/experimental/domains/hierarchy")
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<DomainDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    DomainDto[].class
            );
            return response;
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to retrieve domains: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a domain by name.
     *
     * @param domainName The name of the domain
     * @return The requested DomainDto
     * @throws RuntimeException if the request fails or authentication fails
     */
    public DomainDto getDomain(String domainName) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain")
                    .queryParam("name", domainName)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to retrieve domain: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a specific attribute of a domain.
     *
     * @param domainName    The name of the domain
     * @param attributeName The name of the attribute to retrieve
     * @return The requested DomainDto containing only the specified attribute
     * @throws RuntimeException if the request fails or authentication fails
     */
    public DomainDto getDomainAttribute(String domainName, String attributeName) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, attributeName)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    DomainDto.class
            );
            log.info("Response status : {} body : {}", response.getStatusCode().value(),
                    HttpStatus.valueOf(response.getStatusCode().value()).getReasonPhrase());

            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to retrieve domain attribute: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new domain with a reduced set of attributes.
     *
     * @param domainDto The domain to create
     * @return The created DomainDto
     * @throws RuntimeException if the request fails or authentication fails
     */
    public DomainDto createDomain(DomainDto domainDto) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain")
                    .toUriString();
            HttpEntity<DomainDto> request = new HttpEntity<>(domainDto, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to create domain: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new domain with all attributes.
     *
     * @param domainDto The domain to create
     * @return The created DomainDto
     * @throws RuntimeException if the request fails or authentication fails
     */
    public DomainDto createDomainComplete(DomainDto domainDto) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain/complete")
                    .toUriString();
            HttpEntity<DomainDto> request = new HttpEntity<>(domainDto, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to create domain: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing domain with a reduced set of attributes.
     *
     * @param domainName The name of the domain to update
     * @param domainDto  The updated domain data
     * @return The updated DomainDto
     * @throws RuntimeException if the request fails or authentication fails
     */
    public DomainDto updateDomain(String domainName, DomainDto domainDto) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain")
                    .queryParam("name", domainName)
                    .toUriString();
            HttpEntity<DomainDto> request = new HttpEntity<>(domainDto, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update domain: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing domain with all attributes.
     *
     * @param domainName           The name of the domain to update
     * @param domainDto            The updated domain data
     * @param recursive            Whether to apply changes recursively to sub-domains
     * @return The updated DomainDto
     * @throws RuntimeException if the request fails or authentication fails
     */
    public DomainDto updateDomainComplete(String domainName, DomainDto domainDto, boolean recursive) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain/complete")
                    .queryParam("name", domainName)
                    .queryParam("recursive", recursive)
                    .toUriString();
            HttpEntity<DomainDto> request = new HttpEntity<>(domainDto, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update domain: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a domain.
     *
     * @param domainName The name of the domain to delete
     * @param recursive  Whether to delete sub domains recursively
     * @throws RuntimeException if the request fails or authentication fails
     */
    public void deleteDomain(String domainName, boolean recursive) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domain")
                    .queryParam("name", domainName)
                    .queryParam("recursive", recursive)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
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
     * @return The updated DomainDto
     * @throws RuntimeException if the request fails or authentication fails
     */
    public DomainDto updateSalt(String domainName, String newSalt, boolean allowEmpty) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "salt")
                    .queryParam("salt", newSalt)
                    .queryParam("allowEmpty", allowEmpty)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<DomainDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    request,
                    DomainDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update salt: " + e.getMessage(), e);
        }
    }
}