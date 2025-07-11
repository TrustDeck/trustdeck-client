package org.trustdeck.ace.client.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.ace.client.model.Pseudonym;
import org.trustdeck.ace.client.util.AceClientUtil;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class PseudonymConnector {

    private final String serviceUrl;
    private final RestTemplate restTemplate;
    private final Keycloak keycloakClient;
    private final AceClientUtil aceClientUtil;

    public PseudonymConnector(String serviceUrl, String keycloakUrl, String realm, String clientId, String clientSecret, String userName, String password) {
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

    public ResponseEntity<List<Pseudonym>> createPseudonymBatch(String domainName, boolean omitPrefix, List<Pseudonym> pseudonymList) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .queryParam("omitPrefix", omitPrefix)
                    .toUriString();
            HttpEntity<List<Pseudonym>> request = new HttpEntity<>(pseudonymList, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Pseudonym[]> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Pseudonym[].class);
            Pseudonym[] body = response.getBody();
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(body != null ? Arrays.asList(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to create pseudonym batch: " + e.getMessage(), e);
        }
    }
    public ResponseEntity<List<Pseudonym>> createPseudonym(String domainName, Pseudonym pseudonym, boolean omitPrefix) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("omitPrefix", omitPrefix)
                    .toUriString();
            HttpEntity<Pseudonym> request = new HttpEntity<>(pseudonym, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Pseudonym[]> response = restTemplate.exchange(url, HttpMethod.POST, request, Pseudonym[].class);
            Pseudonym[] body = response.getBody();
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(body != null ? Arrays.asList(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to create pseudonym: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<List<Pseudonym>> getLinkedPseudonyms(String sourceDomain, String targetDomain, String sourceIdentifier, String sourceIdType, String sourcePsn) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serviceUrl)
                    .path("api/pseudonymization/domains/linked-pseudonyms")
                    .queryParam("sourceDomain", sourceDomain)
                    .queryParam("targetDomain", targetDomain);
            if (sourceIdentifier != null) builder.queryParam("sourceIdentifier", sourceIdentifier);
            if (sourceIdType != null) builder.queryParam("sourceIdType", sourceIdType);
            if (sourcePsn != null) builder.queryParam("sourcePsn", sourcePsn);
            String url = builder.toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Pseudonym[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Pseudonym[].class);
            Pseudonym[] body = response.getBody();
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(body != null ? Arrays.asList(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to get linked pseudonyms: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<List<Pseudonym>> getPseudonymByIdentifier(String domainName, String identifier, String idType) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("id", identifier)
                    .queryParam("idType", idType)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Pseudonym[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Pseudonym[].class);
            Pseudonym[] body = response.getBody();
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(body != null ? Arrays.asList(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<List<Pseudonym>> getPseudonymByPsn(String domainName, String psn) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("psn", psn)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Pseudonym[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Pseudonym[].class);
            Pseudonym[] body = response.getBody();
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(body != null ? Arrays.asList(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to retrieve pseudonym: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<List<Pseudonym>> getPseudonymBatch(String domainName) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Pseudonym[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Pseudonym[].class);
            Pseudonym[] body = response.getBody();
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(body != null ? Arrays.asList(body) : List.of());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to fetch pseudonym batch: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Void> updatePseudonymBatch(String domainName, List<Pseudonym> pseudonymList) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .toUriString();
            HttpEntity<List<Pseudonym>> request = new HttpEntity<>(pseudonymList, aceClientUtil.createHeaders(keycloakClient));
            return restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update pseudonym batch: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Pseudonym> updatePseudonymCompleteByIdentifier(String domainName, Pseudonym pseudonym, String identifier, String idType) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
                    .queryParam("id", identifier)
                    .queryParam("idType", idType)
                    .toUriString();
            HttpEntity<Pseudonym> request = new HttpEntity<>(pseudonym, aceClientUtil.createHeaders(keycloakClient));
            return restTemplate.exchange(url, HttpMethod.PUT, request, Pseudonym.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update complete pseudonym: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Pseudonym> updatePseudonymCompleteByPsn(String domainName, Pseudonym pseudonym, String psn) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "complete")
                    .queryParam("psn", psn)
                    .toUriString();
            HttpEntity<Pseudonym> request = new HttpEntity<>(pseudonym, aceClientUtil.createHeaders(keycloakClient));
            return restTemplate.exchange(url, HttpMethod.PUT, request, Pseudonym.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update complete pseudonym: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Pseudonym> updatePseudonymByIdentifier(String domainName, String identifier, String idType, Pseudonym pseudonym) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("id", identifier)
                    .queryParam("idType", idType)
                    .toUriString();
            HttpEntity<Pseudonym> request = new HttpEntity<>(pseudonym, aceClientUtil.createHeaders(keycloakClient));
            return restTemplate.exchange(url, HttpMethod.PUT, request, Pseudonym.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Pseudonym> updatePseudonymByPsn(String domainName, String psn, Pseudonym pseudonym) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym")
                    .queryParam("psn", psn)
                    .toUriString();
            HttpEntity<Pseudonym> request = new HttpEntity<>(pseudonym, aceClientUtil.createHeaders(keycloakClient));
            return restTemplate.exchange(url, HttpMethod.PUT, request, Pseudonym.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to update pseudonym: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Void> deletePseudonymBatch(String domainName) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonyms")
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            return restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to delete pseudonym batch: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<Void> deletePseudonym(String domainName, String identifier, String idType, String psn) {
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
            return restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to delete pseudonym: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<String> validatePseudonym(String domainName, String psn) {
        try {
            String url = UriComponentsBuilder.fromUriString(serviceUrl)
                    .pathSegment("api", "pseudonymization", "domains", domainName, "pseudonym", "validation")
                    .queryParam("psn", psn)
                    .toUriString();
            HttpEntity<?> request = new HttpEntity<>(aceClientUtil.createHeaders(keycloakClient));
            return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to validate pseudonym: " + e.getMessage(), e);
        }
    }
}