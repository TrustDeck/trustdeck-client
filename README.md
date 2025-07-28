# TrustDeck Client Library 
A Client library that provides access to the APIs of the TrustDeck services.

## Overview

The TrustDeck Client Library serves as a client-side interface to the TrustDeck APIs, providing endpoints for:

- Domain management (create, read, update, delete, list all)
- Pseudonym operations (creation, read, update, delete)
- Person management (create, read, update, delete, search)

## Requirements

- Java 21 or later
- Maven 3.6+
- Spring
- Keycloak authentication server
- TrustDeck service

## Getting Started

### Configuration

Create a new configuration object:

    ```java
	TrustDeckClientConfig config = TrustDeckClientConfig.builder()
        		.serviceUrl("https://trustdeck.server.com")
    			.keycloakUrl("https://keycloak.server.com")
    			.realm("production")
    			.clientId("trustdeck")
    			.clientSecret("clientSecret")
    			.userName("testuser")
    			.password("testuserpassword")
    			.build();
     
     ```

### Concrete Usage Example


    ```java
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
    
     // Build a domain object
     Domain domain = Domain.builder().name("TestDomain").prefix("TD-").build();

     // Create new domain
     Domain createdDomain = trustDeck.domains().create(domain);
     
     // There are three ways to create a new pseudonym object
     // 1. Only provide an identifier item and create pseudonym
     IdentifierItem identifierItem1 = IdentifierItem.builder().identifier("TestID1").idType("TestType").build();
     
     // Create new pseudonym by only providing the identifier item
     Pseudonym createdPseudonym1 = trustDeck.pseudonyms(domain.getName()).create(identifierItem1, false);
     
     // 2. Directly use the identifier and idType
     Pseudonym createdPseudonym2 = trustDeck.pseudonyms(domain.getName()).create("TestID2", "TestType", false)
     
     // 3. Provide more information besides the identifier/idType alone
     IdentifierItem identifierItem2 = IdentifierItem.builder().identifier("TestID3").idType("TestType").build();

     // Build a slightly more complex pseudonym object
     Pseudonym pseudonym = Pseudonym.builder()
     	.identifierItem(identifierItem2)
     	.validFrom(LocalDateTime.now())
     	.validityTime("1 week")
     	.build();
        
     // Create new pseudonym by providing the slightly more complex pseudonym object
     Pseudonym createdPseudonym2 = trustDeck.pseudonyms(domain.getName()).create(pseudonym, false);
     
     ```

More examples can be found in the [TrustDeckClientExample.java](src/main/java/org/trustdeck/client/TrustDeckClientExample.java) file.

## To run the example usage files

- Set up  the configuration in the TrustDeckClientExample.java file so it can connect to a running TrustDeck instance.
- Run `clean compile exec:java` in the root directory of the repository
- Note: by default the used Slf4j logger will use stderr to print all logging information. If you want to change that, add e.g. `-Dorg.slf4j.simpleLogger.logFile=System.out` to the command, which then looks like this: `clean compile exec:java -Dorg.slf4j.simpleLogger.logFile=System.out`
### How to Use in Your Application

1. Add the TrustDeck Client library as a dependency

```xml
<!-- TrustDeck Client Library -->
<dependency>
    <groupId>org.trustdeck</groupId>
    <artifactId>client</artifactId>
    <version><!-- current client library version --></version>
</dependency>
```

2. Create a configuration object using your connection properties

```java
TrustDeckClientConfig config = TrustDeckClientConfig.builder()
	.serviceUrl("https://trustdeck.server.com")
	.keycloakUrl("https://keycloak.server.com")
	.realm("production")
	.clientId("trustdeck")
	.clientSecret("clientSecret")
	.userName("testuser")
	.password("testuserpassword")
	.build();
```

3. Create a TrustDeck instance

```java
TrustDeckClient trustDeck = new TrustDeckClient(config);
```

4. Call the connector methods.

