/*
 * TrustDeck Client Library
 * Copyright 2026 Armin Müller
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

import java.util.Base64;
import java.util.UUID;

import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.Entity;
import org.trustdeck.client.model.EntityType;
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.model.Project;
import org.trustdeck.client.model.ProjectImage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Minimal example using configuration supplied outside source control.
 *
 * @author Armin Müller
 */
public class TrustDeckClientExample {

	/**
	 * Creates the example entry point.
	 */
	public TrustDeckClientExample() {
		// Nothing to do
	}

	/**
	 * Runs a complete create, read, and cleanup example using the environment-configured client.
	 *
	 * @param args command-line arguments, unused
	 */
	public static void main(String[] args) {
		TrustDeckClientConfig config = TrustDeckClientConfig.builder()
				.serviceUrl(value("TRUSTDECK_SERVICE_URL"))
				.keycloakUrl(value("TRUSTDECK_KEYCLOAK_URL"))
				.realm(value("TRUSTDECK_REALM"))
				.clientId(value("TRUSTDECK_CLIENT_ID"))
				.clientSecret(value("TRUSTDECK_CLIENT_SECRET"))
				.userName(value("TRUSTDECK_USERNAME"))
				.password(value("TRUSTDECK_PASSWORD"))
				.build();

		TrustDeckClient client = new TrustDeckClient(config);
		String suffix = Long.toString(System.currentTimeMillis());
		String projectAbbreviation = "EX" + suffix.substring(suffix.length() - 8);
		String projectDomainName = "example-domain-" + suffix;
		String baseTypeName = "ExampleBaseType" + suffix;
		String entityTypeName = "ExampleEntityType" + suffix;
		UUID entityId = null;
		String pseudonymValue = null;
		int sleepTime = 1000;

		try {
			System.out.println("--- Examples for the usage of the client library of TrustDeck. ---");
			System.out.println("\nEstablishing connection to the backend at " + config.getServiceUrl() + ".");
			System.out.println(" - Health of the connected backend: " + client.health().getStatus());

			sleep(sleepTime);
			System.out.println("\nCreate a project and use its abbreviation for all project-scoped requests.");
			Project project = Project.builder()
					.name("TrustDeck Client Example " + suffix)
					.abbreviation(projectAbbreviation)
					.storeEntities(true)
					.storePseudonyms(true)
					.build();
			client.projects().create(project);
			System.out.println(" - Project: " + client.projects().get(projectAbbreviation));

			sleep(sleepTime);
			System.out.println("\nUpload and read a small, valid 1x1 PNG image for the project.");
			ProjectImage image = ProjectImage.builder()
					.data(Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="))
					.mimeType("image/png")
					.filename("example.png")
					.build();
			client.projectImages(projectAbbreviation).create(image);
			System.out.println(" - Project image: " + client.projectImages(projectAbbreviation).get());

			sleep(sleepTime);
			System.out.println("\nCreate and read a globally available base entity type.");
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode typeDefinition = mapper.createObjectNode();
			typeDefinition.putArray("attributes")
					.addObject()
					.put("name", "example")
					.put("type", "string")
					.put("required", false)
					.put("linkage", false);
			EntityType baseType = EntityType.builder()
					.name(baseTypeName)
					.version("1.0")
					.isBaseType(true)
					.typeDefinition(typeDefinition)
					.build();
			client.baseEntityTypes().create(baseType);
			System.out.println(" - Base entity type: " + client.baseEntityTypes().get(baseTypeName));

			sleep(sleepTime);
			System.out.println("\nCreate and read a domain that will own the example pseudonym.");
			Domain domain = Domain.builder()
					.name(projectDomainName)
					.prefix("EX" + suffix.substring(suffix.length() - 4))
					.projectAbbreviation(projectAbbreviation)
					.build();
			client.domains().create(domain);
			System.out.println(" - Domain: " + client.domains().get(projectDomainName));

			sleep(sleepTime);
			System.out.println("\nCreate and read a project-scoped entity type.");
			EntityType entityType = EntityType.builder()
					.name(entityTypeName)
					.version("1.0")
					.baseTypeName(baseTypeName)
					.typeDefinition(typeDefinition)
					.build();
			client.entityTypes(projectAbbreviation).create(entityType);
			System.out.println(" - Entity type: " + client.entityTypes(projectAbbreviation).get(entityTypeName));

			sleep(sleepTime);
			System.out.println("\nCreate and read an entity, then demonstrate the related pseudonym lookup.");
			JsonNode entityData = mapper.createObjectNode().put("example", "value");
			Entity entity = Entity.builder().data(entityData).build();
			
			Entity createdEntity = client.entities(projectAbbreviation, entityTypeName).create(entity);
			entityId = createdEntity.getTrustdeckID();
			System.out.println(" - Entity: " + client.entities(projectAbbreviation, entityTypeName).get(entityId));

			sleep(sleepTime);
			System.out.println("\nCreate, read, and validate a pseudonym in the example domain.");
			String identifier = "example-" + suffix;
			String idType = "example-id";
			
			Pseudonym pseudonym = client.pseudonyms(projectDomainName).create(identifier, idType);
			pseudonymValue = pseudonym.getPsn();
			System.out.println(" - Pseudonym: " + client.pseudonyms(projectDomainName).get(pseudonymValue));
			System.out.println(" - Pseudonym valid: " + client.pseudonyms(projectDomainName).validate(pseudonymValue));
			
			sleep(sleepTime);
			System.out.println("\n--- Successfully completed the examples ---");
		} finally {
			System.out.println("\nDelete resources in reverse dependency order, continuing if one cleanup request fails.");
			String createdPseudonym = pseudonymValue;
			UUID createdEntity = entityId;

			if (pseudonymValue != null) {
				cleanup("pseudonym", () -> client.pseudonyms(projectDomainName).delete(createdPseudonym));
			}

			if (entityId != null) {
				cleanup("entity", () -> client.entities(projectAbbreviation, entityTypeName).delete(createdEntity));
			}
			
			cleanup("project image", () -> client.projectImages(projectAbbreviation).delete());
			cleanup("entity type", () -> client.entityTypes(projectAbbreviation).delete(entityTypeName));
			cleanup("domain", () -> client.domains().delete(projectDomainName, false));
			
			// The TrustDeck API currently exposes no delete operation for base entity types
			cleanup("project", () -> client.projects().delete(projectAbbreviation));
			
			System.out.println("\nDone with removing resources.");
		}
	}

	/**
	 * Reads a configuration value from a system property or environment variable.
	 */
	private static String value(String key) {
		String value = System.getProperty(key);

		return value == null ? System.getenv(key) : value;
	}

	/**
	 * Attempts a cleanup operation without preventing subsequent cleanup operations.
	 *
	 * @param resource resource being deleted
	 * @param operation cleanup operation
	 */
	private static void cleanup(String resource, Runnable operation) {
		try {
			operation.run();
			System.out.println(" - Deleted " + resource + ".");
		} catch (RuntimeException exception) {
			System.out.println(" - Could not delete " + resource + ": " + exception.getMessage());
		}
	}
	
	private static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
