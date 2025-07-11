package org.trustdeck.ace.client.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.trustdeck.ace.client.model.Person;
import org.trustdeck.ace.client.util.AceClientUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PersonConnector {

    private final String serviceUrl;
    private final RestTemplate restTemplate;
    private final Keycloak keycloakClient;
    private final AceClientUtil aceClientUtil;

    public PersonConnector(String serviceUrl, String keycloakUrl, String realm, String clientId, String clientSecret, String userName, String password) {
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

    // src/main/java/org/trustdeck/ace/client/service/PersonConnector.java
    public ResponseEntity<Void> createPerson(Person newPerson) {
        try {
            String url = serviceUrl + "api/registration/person";
            HttpEntity<Person> request = new HttpEntity<>(newPerson, aceClientUtil.createHeaders(keycloakClient));
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Void.class
            );
            log.info("Person created successfully: {}", newPerson);
            return response;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                throw new RuntimeException("Authorisation issue, please verify token: " + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to create person: " + e.getMessage(), e);
        }
    }

    // http://localhost:8080/api/registration/person?q=Maria
 }