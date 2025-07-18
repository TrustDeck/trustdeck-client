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

package org.trustdeck.client;

import lombok.extern.slf4j.Slf4j;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.model.Algorithm;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.Person;
import org.trustdeck.client.model.Pseudonym;

/**
 * Example demonstrating usage of the TrustDeck Client Library.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Slf4j
public class TrustDeckClientExample {

	/**
	 * Entry point into the example.
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        log.info("Starting TrustDeck Client Library example...");
        
        // Set config
        TrustDeckClientConfig config = new TrustDeckClientConfig()
        		.serviceUrl("https://trustdeck.server.com")
    			.keycloakUrl("https://keycloak.server.com")
    			.realm("production")
    			.clientId("trustdeck")
    			.clientSecret("clientSecret")
    			.userName("testuser")
    			.password("testuserpassword");
        
        // Create client instance
        TrustDeckClient trustDeck = new TrustDeckClient(config);
        
        // Build a domain object
    	String domainName = "TestDomain-" + System.currentTimeMillis();
        Domain newDomain = new Domain();
        newDomain.setName(domainName);
        newDomain.setPrefix("TD-");

        // Create new domain
        if (trustDeck.domains().createDomain(newDomain)) {
            log.info("Successfully created domain '{}'.", domainName);
        } else {
        	log.warn("Failed creating domain '{}'.", domainName);
        }

        // Retrieve and log the created domain
        Domain fetchedDomain = trustDeck.domains().getDomain(domainName);
        log.info("The newly created domain fetched from TrustDeck: {}.", fetchedDomain);

        // Build pseudonym object
        String pseudonymId = "TestID" + System.currentTimeMillis();
        String pseudonymIdType = "TestType";
        Pseudonym pseudonym = new Pseudonym();
        pseudonym.setId(pseudonymId);
        pseudonym.setIdType(pseudonymIdType);
        pseudonym.setValidityTime("1 week");

        // Create new pseudonym
        Pseudonym createdPseudonym = trustDeck.pseudonyms().createPseudonym(domainName, pseudonym, true);
        if (createdPseudonym != null) {
            log.info("Successfully created pseudonym '{}' in domain '{}'.", createdPseudonym, domainName);
        } else {
        	log.warn("Failed creating pseudonym '{}' in domain '{}'.", pseudonym, domainName);
        }

        // Delete the created pseudonym
        if (trustDeck.pseudonyms().deletePseudonym(domainName, pseudonymId, pseudonymIdType, null)) {
            log.info("Successfully deleted pseudonym with id '{}' in domain '{}'.", pseudonymId, domainName);
        } else {
        	log.info("Failed deleting pseudonym with id '{}' in domain '{}'.", pseudonymId, domainName);
        }

        // Delete the created domain
        if (trustDeck.domains().deleteDomain(domainName, true)) {
            log.info("Successfully deleted domain '{}'.", domainName);
        } else {
        	log.warn("Failed deleting domain '{}'.", domainName);
        }
        
        // Build basic algorithm object
        Algorithm algo = new Algorithm();
        algo.setName("RANDOM_NUM");
        
        // Build person object
        String identifier = "" + System.currentTimeMillis();
        Person person = new Person();
        person.setFirstName("Max");
        person.setLastName("Mustermann");
        person.setAdministrativeGender("M");
        person.setDateOfBirth("1970-01-01");
        person.setIdentifier(identifier);
        person.setAlgorithm(algo);
        
        // Create person
        if (trustDeck.persons().createPerson(person)) {
            log.info("Successfully created person '{}'.", person);
        } else {
        	log.warn("Failed creating person '{}'.", person);
        }
        
        // Search person
        Person foundPerson = trustDeck.persons().searchPersons(identifier).get(0);
        
        if (foundPerson!= null && foundPerson.getIdentifier().equals(identifier)) {
            log.info("Successfully found person '{}'.", foundPerson);
        } else {
        	log.warn("Failed finding person with identifier '{}'.", identifier);
        }

        log.info("Finished TrustDeck Client Library example.");
    }
}