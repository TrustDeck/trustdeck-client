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
import org.trustdeck.client.service.DBMaintenance;
import org.trustdeck.client.exception.TrustDeckClientLibraryException;


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
                .serviceUrl("http://localhost:8080")
                .keycloakUrl("http://localhost:8081")
                .realm("development")
                .clientId("trustdeck")
                .clientSecret("1h6T3Dnx45hrd4pgv7YdcIfP9GRarbpN")
                .userName("test")
                .password("test")
                .build();

        // Create client instance
        TrustDeckClient trustDeck = new TrustDeckClient(config);

        // Check connection to TrustDeck
        if (trustDeck.ping()) {
            log.info("Successfully pinged TrustDeck.");
        } else {
            log.error("Failed to connect to TrustDeck.");
        }

        //create Domain
        Domain domain = Domain.builder()
                .name("TestStudie")
                .prefix("TAD-")
                .build();

        //DBMaintenance

        // Setup DBMaintenance service
        DBMaintenance dbMaintenance = trustDeck.dbMaintenance();

        // Clear tables
        try {
            dbMaintenance.clearTables();
            log.info("Successfully cleared tables.");
        } catch (TrustDeckClientLibraryException e) {
            log.error("Failed to clear tables: {}", e.getMessage());
        }

        // Delete rights and roles for a domain
        try {
            dbMaintenance.deleteDomainRightsAndRoles(domain);
            log.info("Successfully deleted roles for domain '{}'.", domain.getName());
        } catch (TrustDeckClientLibraryException e) {
            log.error("Failed to delete roles: {}", e.getMessage());
        }

        // Get storage usage for a table
        try {
            String storage = dbMaintenance.getStorage("pseudonym");
            log.info("Storage usage for 'pseudonym' table: {}", storage);
        } catch (TrustDeckClientLibraryException e) {
            log.error("Failed to get storage usage: {}", e.getMessage());
        }
    }}

