# TrustDeck Client Library 
A Client library that provides access to the APIs of the TrustDeck services.

> **Note:** This project is currently under active development. APIs and features may change.

## Overview

The TrustDeck Client Library serves as a client-side interface to the TrustDeck APIs, providing endpoints for:

- Domain management (create, read, update, delete, list all)
- Pseudonym operations (creation, read, update, delete)
- Person management (create, read, update, delete)

## Requirements

- Java 21 or later
- Maven 3.6+
- Spring
- Keycloak authentication server
- TrustDeck service

## Getting Started

### Configuration

Instantiate using TrustDeckClientConfigBuilder:

    ```java
	@Autowired
	private TrustDeckClientConfigBuilder trustDeckClientConfigBuilder;
    
	trustDeckClientConfigBuilder
		.serviceUrl("https://trustdeck.server.com")
		.keycloakUrl("https://keycloak.server.com")
		.realm("production")
		.clientId("trustdeck")
		.clientSecret("clientSecret")
		.userName("testuser")
		.password("testuserpassword");
     
     ```

### Concrete Usage Example


    ```java
	@Autowired
	private TrustDeckClientConfigBuilder trustDeckClientConfigBuilder;
    
	@Autowired
	private DomainConnector domainConnector;

	@Autowired
	private PseudonymConnector pseudonymConnector;
    
	trustDeckClientConfigBuilder
		.serviceUrl("https://trustdeck.server.com")
		.keycloakUrl("https://keycloak.server.com")
		.realm("production")
		.clientId("trustdeck")
		.clientSecret("clientSecret")
		.userName("testuser")
		.password("testuserpassword");
    
     Domain newDomain = new Domain();
     newDomain.setName("TestDomain");
     newDomain.setPrefix("TD-");
     ResponseEntity<Domain> createdDomainResponse = domainConnector.createDomain(newDomain);
     if (createdDomainResponse.getStatusCode().is2xxSuccessful())
         log.info("Successfully created domain.");
         
     Pseudonym newPseudonym = new Pseudonym();
     newPseudonym.setId(pseudonymId);
     newPseudonym.setIdType(pseudonymIdType);
     newPseudonym.setValidityTime("1 week");
     ResponseEntity<Pseudonym[]> createdPseudonym = pseudonymConnector.createPseudonym("TestDomain", pseudonym, true);
     if (createdPseudonym.getStatusCode().is2xxSuccessful())
         log.info("Successfully created pseudonym.");
     
     ```

## To run the example usage files

- TrustDeckClientExample.java
- `mvn spring-boot:run`

### How to Use in Your Application

1. Add the TrustDeck Client library as a dependency.
2. Configure your connection properties.
3. Autowire the connector classes.
4. Call the connector methods.

