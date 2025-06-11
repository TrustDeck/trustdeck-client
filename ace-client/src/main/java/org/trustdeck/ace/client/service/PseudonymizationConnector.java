/*
 * Pseudonymization Connector Library
 * A client library for interacting with the pseudonymization REST service.
 * Requires dependencies: spring-web, jackson-databind, keycloak-admin-client, lombok
 */
package org.trustdeck.ace.client.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.ace.client.dto.PseudonymDto;
import org.springframework.http.HttpStatus;
import org.trustdeck.ace.client.util.AceClientUtil;
import java.util.List;

/**
 * Client library for interacting with a REST-based pseudonymization service.
 * Handles HTTP requests for pseudonym operations and Keycloak authentication.
 */
@Slf4j
public class PseudonymizationConnector {

    private final String serviceUrl;
    private final RestTemplate restTemplate;
    private final Keycloak keycloakClient;
    private final AceClientUtil aceClientUtil;

    public PseudonymizationConnector(String serviceUrl, String keycloakUrl, String realm,
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
     * Creates a batch of pseudonym records.
     *
     * @param domainName       The domain for the pseudonyms.
     * @param omitPrefix       If true, omits the domain prefix.
     * @param pseudonymDtoList List of pseudonym data to create.
     * @return ResponseEntity with created pseudonyms and HTTP 201 Created status.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> createPseudonymBatch(String domainName, boolean omitPrefix, List<PseudonymDto> pseudonymDtoList) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .queryParam("omitPrefix", omitPrefix)
                    .toUriString();
            HttpEntity<List<PseudonymDto>> request = new HttpEntity<>(pseudonymDtoList, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to create pseudonym batch: " + e.getMessage());
        }
    }

    /**
     * Creates a single pseudonym record.
     *
     * @param domainName   The domain for the pseudonym.
     * @param pseudonymDto The pseudonym data.
     * @param omitPrefix   If true, omits the domain prefix.
     * @return ResponseEntity with created pseudonym and HTTP 201 Created status.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> createPseudonym(String domainName, PseudonymDto pseudonymDto, boolean omitPrefix) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("omitPrefix", omitPrefix)
                    .toUriString();
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to create pseudonym: " + e.getMessage());
        }
    }

    /**
     * Retrieves linked pseudonyms between domains.
     *
     * @param sourceDomain     The source domain.
     * @param targetDomain     The target domain.
     * @param sourceIdentifier The source identifier (optional).
     * @param sourceIdType     The source idType (optional).
     * @param sourcePsn        The source pseudonym (optional).
     * @return ResponseEntity with linked pseudonyms.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> getLinkedPseudonyms(String sourceDomain, String targetDomain, String sourceIdentifier, String sourceIdType, String sourcePsn) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domains/linked-pseudonyms")
                    .queryParam("sourceDomain", sourceDomain)
                    .queryParam("targetDomain", targetDomain);
            if (sourceIdentifier != null) builder.queryParam("sourceIdentifier", sourceIdentifier);
            if (sourceIdType != null) builder.queryParam("sourceIdType", sourceIdType);
            if (sourcePsn != null) builder.queryParam("sourcePsn", sourcePsn);
            String url = builder.toUriString();
            HttpEntity<PseudonymDto> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to get linked pseudonyms: " + e.getMessage());
        }
    }

    /**
     * Retrieves a pseudonym by identifier and idType.
     *
     * @param domainName The domain name.
     * @param identifier The record identifier.
     * @param idType     The identifier type.
     * @return ResponseEntity with matching pseudonyms.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> getPseudonymByIdentifier(String domainName, String identifier, String idType) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("id", identifier)
                    .queryParam("idType", idType)
                    .toUriString();
            HttpEntity<PseudonymDto> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage());
        }
    }

    /**
     * Retrieves a pseudonym by pseudonym value.
     *
     * @param domainName The domain name.
     * @param psn        The pseudonym value.
     * @return ResponseEntity with matching pseudonyms.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> getPseudonymByPsn(String domainName, String psn) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("psn", psn)
                    .toUriString();
            HttpEntity<PseudonymDto> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage());
        }
    }

    /**
     * Retrieves all pseudonyms in a domain.
     *
     * @param domainName The domain name.
     * @return ResponseEntity with pseudonym list.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> getPseudonymBatch(String domainName) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, PseudonymDto[].class);
            PseudonymDto[] body = response.getBody();
            return ResponseEntity.ok(body != null ? List.of(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to fetch pseudonym batch: " + e.getMessage());
        }
    }

    /**
     * Updates a batch of pseudonym records.
     *
     * @param domainName       The domain name.
     * @param pseudonymDtoList List of updated pseudonym data.
     * @return ResponseEntity indicating success.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymBatch(String domainName, List<PseudonymDto> pseudonymDtoList) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .toUriString();
            HttpEntity<List<PseudonymDto>> request = new HttpEntity<>(pseudonymDtoList, aceClientUtil.createHeaders(keycloakClient));
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update pseudonym batch: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym by identifier and idType, replacing the entire record.
     *
     * @param domainName   The domain name.
     * @param pseudonymDto The updated pseudonym data.
     * @param identifier   The record identifier.
     * @param idType       The identifier type.
     * @return ResponseEntity with updated pseudonym.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymCompleteByIdentifier(String domainName, PseudonymDto pseudonymDto,
                                                                 String identifier, String idType) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
                    .queryParam("id", identifier)
                    .queryParam("idType", idType)
                    .toUriString();
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, PseudonymDto.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update complete pseudonym: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym by pseudonym value, replacing the entire record.
     *
     * @param domainName   The domain name.
     * @param pseudonymDto The updated pseudonym data.
     * @param psn          The pseudonym value.
     * @return ResponseEntity with updated pseudonym.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymCompleteByPsn(String domainName, PseudonymDto pseudonymDto, String psn) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
                    .queryParam("psn", psn)
                    .toUriString();
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, PseudonymDto.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update complete pseudonym: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym by identifier and idType.
     *
     * @param domainName   The domain name.
     * @param identifier   The record identifier.
     * @param idType       The identifier type.
     * @param pseudonymDto The updated pseudonym data.
     * @return ResponseEntity with updated pseudonym.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymByIdentifier(String domainName, String identifier, String idType, PseudonymDto pseudonymDto) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("id", identifier)
                    .queryParam("idType", idType)
                    .toUriString();
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, PseudonymDto.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage());
        }
    }

    /**
     * Updates a pseudonym by pseudonym value.
     *
     * @param domainName   The domain name.
     * @param psn          The pseudonym value.
     * @param pseudonymDto The updated pseudonym data.
     * @return ResponseEntity with updated pseudonym.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> updatePseudonymByPsn(String domainName, String psn, PseudonymDto pseudonymDto) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("psn", psn)
                    .toUriString();
            HttpEntity<PseudonymDto> request = new HttpEntity<>(pseudonymDto, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<PseudonymDto> response = restTemplate.exchange(
                    url, HttpMethod.PUT, request, PseudonymDto.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage());
        }
    }

    /**
     * Deletes all pseudonyms in a domain.
     *
     * @param domainName The domain name.
     * @return ResponseEntity indicating success.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> deletePseudonymBatch(String domainName) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to delete pseudonym batch: " + e.getMessage());
        }
    }

    /**
     * Deletes a pseudonym by identifier, idType, or pseudonym value.
     *
     * @param domainName The domain name.
     * @param identifier The record identifier (optional).
     * @param idType     The identifier type (optional).
     * @param psn        The pseudonym value (optional).
     * @throws RuntimeException If the request fails.
     */
    public void deletePseudonym(String domainName, String identifier, String idType, String psn) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym");
            if (identifier != null && idType != null) {
                builder.queryParam("id", identifier)
                        .queryParam("idType", idType);
            } else if (psn != null) {
                builder.queryParam("psn", psn);
            } else {
                throw new IllegalArgumentException("Either identifier and idType or psn must be provided");
            }
            String url = builder.toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to delete pseudonym: " + e.getMessage());
        }
    }

    /**
     * Validates a pseudonym value.
     *
     * @param domainName The domain name.
     * @param psn        The pseudonym value.
     * @return ResponseEntity with validation result.
     * @throws RuntimeException If the request fails.
     */
    public ResponseEntity<?> validatePseudonym(String domainName, String psn) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "validation")
                    .queryParam("psn", psn)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException(" Authorisation issue , please verify token" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to validate pseudonym: " + e.getMessage());
        }
    }
}