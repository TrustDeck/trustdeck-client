/*
 * ACE (Advanced Confidentiality Engine) Client Library Example
 * Copyright 2025 Chethan Chinnabhandara Nagaraj & Armin Müller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustdeck.ace.client.examples;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.trustdeck.ace.client.config.AceClientConfig;
import org.trustdeck.ace.client.config.AceClientProperties;
import org.trustdeck.ace.client.model.Domain;
import org.trustdeck.ace.client.model.Pseudonym;
import org.trustdeck.ace.client.service.DomainConnector;
import org.trustdeck.ace.client.service.PseudonymConnector;
import org.trustdeck.ace.client.service.PersonConnector;

import java.util.List;


/**
 * Example demonstrating usage of the TrustDeck ACE Client Library without Spring Boot.
 */
@Slf4j
public class AceClientExample {

    public static void main(String[] args) {
        // TODO - pass as env variables
        // Configure properties using builder pattern with values from properties or environment
        AceClientProperties clientProperties = AceClientProperties.builder()
                .serviceUrl("http://localhost:8080")
                .keycloakUrl("http://localhost:8081")
                .realm("development")
                .clientId("trustdeck")
                .clientSecret("1h6T3Dnx45hrd4pgv7YdcIfP9GRarbpN")
                .userName("test")
                .password("test")
                .build();

        AceClientConfig config = new AceClientConfig(clientProperties);
        DomainConnector domainConnector = config.createDomainConnector();
        PseudonymConnector pseudonymConnector = config.createPseudonymizationConnector();
        PersonConnector personConnector = config.createPersonConnector();

        log.info("Starting AceClientExample...");
        runExample(domainConnector, pseudonymConnector, personConnector);
    }

    /**
     * Runs a single-threaded example of domain and pseudonym operations.
     */
    private static void runExample(DomainConnector domainConnector, PseudonymConnector pseudonymConnector, PersonConnector personConnector) {
        try {


            // Prepare domain object with a unique name and prefix
            String domainName = "TestDomain-" + System.currentTimeMillis();
            Domain newDomain = new Domain();
            newDomain.setName(domainName);
            newDomain.setPrefix("TD-");

            // Create new domain
            ResponseEntity<Domain> createdDomain = domainConnector.createDomain(newDomain);
            if (createdDomain != null) {
                log.info("Created domain '{}'", domainName);
            } else {
                throw new RuntimeException("Domain creation not successful for '" + domainName + "'");
            }

            // Retrieve and log the created domain
            ResponseEntity<Domain> fetchedDomain = domainConnector.getDomain(domainName);
            log.info("The created domain is: {}", fetchedDomain);

            // Prepare pseudonym object
            String pseudonymId = "TS-123";
            String pseudonymIdType = "TS";
            Pseudonym pseudonym = new Pseudonym();
            pseudonym.setId(pseudonymId);
            pseudonym.setIdType(pseudonymIdType);
            pseudonym.setValidityTime("1 week");

            // Create new pseudonym
            ResponseEntity<List<Pseudonym>> createdPseudonym = pseudonymConnector.createPseudonym(domainName, pseudonym, true);
            if (createdPseudonym != null) {
                log.info("Successfully created pseudonym '{}' in domain '{}'", createdPseudonym, domainName);
            } else {
                throw new RuntimeException("Pseudonym creation not successful for '" + domainName + "'");
            }

            // Delete the created pseudonym
            ResponseEntity<Void> pseudonymCreationResponse = pseudonymConnector.deletePseudonym(domainName, pseudonymId, pseudonymIdType, null);
            HttpStatusCode pseudonymCreationStatus = pseudonymCreationResponse.getStatusCode();
            log.info("Deleted pseudonym '{}' in domain '{}', successfully status code - {}", pseudonymId, domainName, pseudonymCreationStatus);

            // Delete the created domain
            domainConnector.deleteDomain(domainName, true);
            log.info("Deleted domain '{}' successfully", domainName);

            /**
             *  A typical HDP/DIZ workflow ( IN dev)
             *  Store basic patient data with ID/pseudonym.
             *  Create a domain.
             * 	Generate a secondary/tertiary pseudonym in the domain for an existing pseudonym.
             */


//
//            // Prepare Person DTO
//            Person newPerson = new Person();
//            Algorithm algorithm = new Algorithm();
//            algorithm.setName("RANDOM_NUM");
//            newPerson.setId(123);
//            newPerson.setFirstName("John");
//            newPerson.setLastName("Doe");
//            newPerson.setAdministrativeGender("M");
//            newPerson.setAlgorithm(algorithm);
//
//
//        // Create new Person
//            ResponseEntity<Void> personCreationResponse = personConnector.createPerson(newPerson);
//            HttpStatusCode personCreationStatus = personCreationResponse.getStatusCode();
//
//            if (personCreationStatus.is2xxSuccessful()) {
//                log.info("Person created successfully with status code: {}", personCreationStatus.value());
//            } else {
//                throw new RuntimeException("Person creation failed with status code: " + personCreationStatus.value());
//            }
//
        } catch (Exception e) {
            log.error("Error processing request: {}", e.getMessage(), e);
        }
    }
}