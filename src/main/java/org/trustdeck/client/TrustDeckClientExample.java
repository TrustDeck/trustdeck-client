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

import java.time.LocalDateTime;

import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.model.Algorithm;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.IdentifierItem;
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
        
        // Create configuration object
        TrustDeckClientConfig config = TrustDeckClientConfig.builder()
        		.serviceUrl("https://trustdeck.server.com")
    			.keycloakUrl("https://keycloak.server.com")
    			.realm("production")
    			.clientId("trustdeck")
    			.clientSecret("clientSecret")
    			.userName("testuser")
    			.password("testuserpassword")
    			.build();
        
        // Create client instance
        TrustDeckClient trustDeck = new TrustDeckClient(config);
        
        // Check connection to TrustDeck
        if (trustDeck.ping()) {
        	log.info("Successfully pinged TrustDeck.");
        } else {
        	log.error("Failed to connect to TrustDeck.");
        }
        
        // Build a domain object
        Domain domain = Domain.builder()
        		.name("TestDomain-" + System.currentTimeMillis())
        		.prefix("TD-")
        		.build();

        // Create new domain
        if (trustDeck.domains().create(domain) != null) {
            log.info("Successfully created domain '{}'.", domain.getName());
        } else {
        	log.warn("Failed creating domain '{}'.", domain.getName());
        }

        // Retrieve and log the created domain
        domain = trustDeck.domains().get(domain.getName());
        log.info("The newly created domain fetched from TrustDeck: {}.", domain);
        
        // Build identifier object
        IdentifierItem identifierItem = IdentifierItem.builder()
        		.identifier("TestID-" + System.currentTimeMillis())
        		.idType("TestType")
        		.build();

        // Build a slightly more complex pseudonym object
        Pseudonym pseudonym = Pseudonym.builder()
        		.identifierItem(IdentifierItem.builder().identifier("TestID-" + System.currentTimeMillis()).idType("TestType").build())
        		.validFrom(LocalDateTime.now())
        		.validityTime("1 week")
        		.build();
        
        // Create new pseudonym by only providing the identifier item
        Pseudonym createdPseudonym1 = trustDeck.pseudonyms(domain.getName()).create(identifierItem, true);

        // Create new pseudonym by providing the more complex object
        Pseudonym createdPseudonym2 = trustDeck.pseudonyms(domain.getName()).create(pseudonym, true);
        if (createdPseudonym1 != null && createdPseudonym2 != null) {
            log.info("Successfully created pseudonyms '{}' and '{}' in domain '{}'.", createdPseudonym1, createdPseudonym2, domain.getName());
        } else {
        	log.warn("Failed creating pseudonym for '{}' and '{}' in domain '{}'.", pseudonym, identifierItem, domain.getName());
        }

        // Delete the created pseudonym
        if (trustDeck.pseudonyms(domain.getName()).delete(pseudonym.getIdentifierItem())) {
            log.info("Successfully deleted pseudonym with id '{}' in domain '{}'.", pseudonym.getIdentifierItem().getIdentifier(), domain.getName());
        } else {
        	log.info("Failed deleting pseudonym with id '{}' in domain '{}'.", pseudonym.getIdentifierItem().getIdentifier(), domain.getName());
        }

        // Delete the created domain
        if (trustDeck.domains().delete(domain.getName(), true)) {
            log.info("Successfully deleted domain '{}'.", domain.getName());
        } else {
        	log.warn("Failed deleting domain '{}'.", domain.getName());
        }
        
        // Build person object
        IdentifierItem personIdentifier = IdentifierItem.builder()
        		.identifier(String.valueOf(System.currentTimeMillis()))
        		.idType("personTestIdentifier")
        		.build();
        	
        Person person = Person.builder()
        		.firstName("Max")
        		.lastName("Mustermann")
        		.administrativeGender("M")
        		.dateOfBirth("1970-01-01")
        		.identifierItem(identifierItem)
        		.identifier(personIdentifier.getIdentifier())
        		.idType(personIdentifier.getIdType())
        		.algorithm(Algorithm.builder().name("RANDOM_NUM").build())
        		.build();
        
        // Create person
        Person createdPerson = trustDeck.persons().create(person);
        if (createdPerson != null) {
            log.info("Successfully created person '{}'.", createdPerson);
        } else {
        	log.warn("Failed creating person '{}'.", person);
        }
        
        // Search person
        Person foundPerson = trustDeck.persons().search(personIdentifier.getIdentifier()).get(0);
        if (foundPerson!= null && foundPerson.getIdentifier().equals(personIdentifier.getIdentifier())) {
            log.info("Successfully found person '{}'.", foundPerson);
        } else {
        	log.warn("Failed finding person with identifier '{}'.", personIdentifier.getIdentifier());
        }
        
        // Update person
        person.setFirstName("Erika");
        Person updatedPerson = trustDeck.persons().update(personIdentifier, person);
        if (updatedPerson != null) {
        	log.info("Successfully updated person '{}'.", updatedPerson);
        } else {
        	log.warn("Failed updating person with identifier '{}'.", personIdentifier.getIdentifier());
        }
        
        // Delete person
        if (trustDeck.persons().delete(personIdentifier)) {
        	log.info("Successfully deleted person '{}'.", person);
        } else {
        	log.warn("Failed deleting person with identifier '{}'.", personIdentifier.getIdentifier());
        }

        log.info("Finished TrustDeck Client Library example.");
    }
}