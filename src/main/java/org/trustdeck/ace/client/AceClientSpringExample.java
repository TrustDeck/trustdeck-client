/*
 * ACE (Advanced Confidentiality Engine) Client Library Spring Boot Example
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
package org.trustdeck.ace.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.trustdeck.ace.client.config.AceClientConfig;
import org.trustdeck.ace.client.model.Algorithm;
import org.trustdeck.ace.client.model.Domain;
import org.trustdeck.ace.client.model.Person;
import org.trustdeck.ace.client.model.Pseudonym;
import org.trustdeck.ace.client.service.DomainConnector;
import org.trustdeck.ace.client.service.PersonConnector;
import org.trustdeck.ace.client.service.PseudonymConnector;

import java.util.List;

/**
 * Spring Boot example demonstrating usage of the TrustDeck ACE Client Library.
 */
@SpringBootApplication
@Slf4j
public class AceClientSpringExample implements CommandLineRunner {

    @Autowired
    private AceClientConfig aceClientConfig;

    public static void main(String[] args) {
        SpringApplication.run(AceClientSpringExample.class, args);
    }

    @Bean
    public DomainConnector domainConnector() {
        return aceClientConfig.createDomainConnector();
    }

    @Bean
    public PseudonymConnector pseudonymConnector() {
        return aceClientConfig.createPseudonymizationConnector();
    }

    @Bean
    public PersonConnector personConnector() {
        return aceClientConfig.createPersonConnector();
    }

    @Override
    public void run(String... args) {
        log.info("Starting AceClientSpringExample...");
        runExample(domainConnector(), pseudonymConnector(), personConnector());
    }

    /**
     * Runs a single-threaded example of domain and pseudonym operations.
     */
    private static void runExample(DomainConnector domainConnector, PseudonymConnector pseudonymConnector, PersonConnector personConnector) {
//        try {
        String domainName = "TestDomain-" + System.currentTimeMillis();
        Domain newDomain = new Domain();
        newDomain.setName(domainName);
        newDomain.setPrefix("TD-");

        // Create new domain
        ResponseEntity<Domain> createdDomainResponse = domainConnector.createDomain(newDomain);
        Domain createdDomain = createdDomainResponse.getBody();
        if (createdDomain != null && createdDomainResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Created domain '{}'", domainName);
        } else {
            throw new RuntimeException("Domain creation not successful for '" + domainName + "'");
        }

        // Retrieve and log the created domain
        ResponseEntity<Domain> fetchedDomainResponse = domainConnector.getDomain(domainName);
        Domain fetchedDomain = fetchedDomainResponse.getBody();
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
        if (createdPseudonym != null && createdPseudonym.getStatusCode().is2xxSuccessful()) {
            log.info("Successfully created pseudonym '{}' in domain '{}'", createdPseudonym.getBody(), domainName);
        } else {
            throw new RuntimeException("Pseudonym creation not successful for '" + domainName + "'");
        }

        // Delete the created pseudonym
        ResponseEntity<Void> deletePsnResponse = pseudonymConnector.deletePseudonym(domainName, pseudonymId, pseudonymIdType, null);
        if (deletePsnResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Deleted pseudonym '{}' in domain '{}' successfully", pseudonymId, domainName);
        } else {
            throw new RuntimeException("Pseudonym deletion not successful for '" + pseudonymId + "' in domain '" + domainName + "'");
        }

        // Delete the created domain
        ResponseEntity<Void> deleteDomainResponse = domainConnector.deleteDomain(domainName, true);
        if (deleteDomainResponse.getStatusCode().is2xxSuccessful()) {
            log.info("Deleted domain '{}' successfully", domainName);
        } else {
            throw new RuntimeException("Domain deletion not successful for '" + domainName + "'");
        }



        /**
         *  A typical HDP/DIZ workflow ( IN dev)
         *  Store basic patient data with ID/pseudonym.
         *  Create a domain.
         * 	Generate a secondary/tertiary pseudonym in the domain for an existing pseudonym.
         */
//
//            // Prepare Person and Domain DTOs
//            Person newPerson = new Person();
//            Algorithm algorithm = new Algorithm();
//            algorithm.setName("RANDOM_NUM");
//            newPerson.setId(123);
//            newPerson.setFirstName("John");
//            newPerson.setLastName("Doe");
//            newPerson.setAdministrativeGender("M");
//            newPerson.setAlgorithm(algorithm);
//
//            String domainName = "TestDomain-" + System.currentTimeMillis();
//            Domain testDomain = new Domain();
//            testDomain.setName(domainName);
//            testDomain.setPrefix("TD-");
//
//            // 1.  Create new Person
//            ResponseEntity<Void> personCreationResponse = personConnector.createPerson(newPerson);
//            HttpStatusCode personCreationStatus = personCreationResponse.getStatusCode();
//
//            if (personCreationStatus.is2xxSuccessful()) {
//                log.info("Person created successfully with status code: {}", personCreationStatus.value());
//            } else {
//                throw new RuntimeException("Person creation failed with status code: " + personCreationStatus.value());
//            }
//
//            // 2 . Create new domain
//            ResponseEntity<Domain> testDomainResponse = domainConnector.createDomain(testDomain);
//            Domain createdTestDomain = testDomainResponse.getBody();
//            if (createdTestDomain != null) {
//                log.info("Created domain '{}'", domainName);
//            } else {
//                throw new RuntimeException("Domain creation not successful for '" + domainName + "'");
//            }
//


//            // retrieve the id and idtype
//            String identifier = personConnector.getPerson
//
//            // 3. Generate a secondary/tertiary pseudonym in the domain for an existing pseudonym.
//
//            Pseudonym secondaryPseudonym = new Pseudonym();
//            secondaryPseudonym.setId(); //
//            secondaryPseudonym.setIdType();
//
//            ResponseEntity<List<Pseudonym>> secondaryPsnResponse = pseudonymConnector.createPseudonym(
//                    createdTestDomain.getName(), secondaryPseudonym, true);
//
//
//
//        } catch (Exception e) {
//            log.error("Error processing request: {}", e.getMessage(), e);
//        }
//    }
//
//}

    }
}