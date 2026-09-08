# TrustDeck Client Library

Synchronous Java access to TrustDeck domains, pseudonyms, projects, project images, entity types, generic entities, permissions, users, and health.

## Requirements

The library requires Java 21 or newer. Maven is used to build the project and to run the included example.

## Configuration

Create a `TrustDeckClient` with password-grant settings, preferably loaded from environment variables or a secrets manager. Applications with another OAuth flow may provide an `AccessTokenProvider` instead.

`TrustDeckClientExample` reads the connection details from these environment variables:

- `TRUSTDECK_SERVICE_URL`: URL of the TrustDeck backend
- `TRUSTDECK_KEYCLOAK_URL`: base URL of the Keycloak server
- `TRUSTDECK_REALM`: Keycloak realm
- `TRUSTDECK_CLIENT_ID`: Keycloak client ID
- `TRUSTDECK_CLIENT_SECRET`: Keycloak client secret
- `TRUSTDECK_USERNAME`: Keycloak user name
- `TRUSTDECK_PASSWORD`: Keycloak user password

The example also accepts these values as Java system properties. System properties take precedence over environment variables when both are provided.

The configured Keycloak client must allow Direct Access Grants and have a client secret. The configured user must have the TrustDeck permissions required by the operations being performed.

```java
TrustDeckClientConfig config = TrustDeckClientConfig.builder()
	.serviceUrl(System.getenv("TRUSTDECK_SERVICE_URL"))
	.keycloakUrl(System.getenv("TRUSTDECK_KEYCLOAK_URL"))
	.realm(System.getenv("TRUSTDECK_REALM"))
	.clientId(System.getenv("TRUSTDECK_CLIENT_ID"))
	.clientSecret(System.getenv("TRUSTDECK_CLIENT_SECRET"))
	.userName(System.getenv("TRUSTDECK_USERNAME"))
	.password(System.getenv("TRUSTDECK_PASSWORD"))
	.build();
TrustDeckClient trustdeck = new TrustDeckClient(config);
```

## Running the Example

After setting the connection variables, run the complete example with:

```bash
mvn clean compile exec:java
```

The example performs real requests against the configured backend. It creates uniquely named projects, images, base and project-scoped entity types, domains, entities, and pseudonyms, reads the created resources, validates the pseudonym, and deletes the resources that support deletion in reverse dependency order.

Base entity types currently have no delete operation in the TrustDeck API. The generated base entity type therefore remains after the example finishes and must be removed through an administrative backend operation if necessary.

## Concrete Usage Example

The following example creates a project and domain, then demonstrates the available pseudonym creation styles. The same services can also be used to read, update, search, and delete these resources.

```java
String projectAbbreviation = "research";

Project project = Project.builder()
	.name("Research project")
	.abbreviation(projectAbbreviation)
	.storeEntities(true)
	.storePseudonyms(true)
	.build();
Project createdProject = trustdeck.projects().create(project);

Domain domain = Domain.builder()
	.name("research-domain")
	.prefix("RD")
	.projectAbbreviation(createdProject.getAbbreviation())
	.build();
Domain createdDomain = trustdeck.domains().create(domain);

// Create a pseudonym from an identifier item
IdentifierItem identifierItem = IdentifierItem.builder()
	.identifier("TestID1")
	.idType("TestType")
	.build();
Pseudonym createdPseudonym1 = trustdeck.pseudonyms(createdDomain.getName())
	.create(identifierItem, false);

// Create a pseudonym directly from an identifier and identifier type
Pseudonym createdPseudonym2 = trustdeck.pseudonyms(createdDomain.getName())
	.create("TestID2", "TestType", false);

// Create a pseudonym with additional validity information
Pseudonym pseudonym = Pseudonym.builder()
	.identifierItem(IdentifierItem.builder()
		.identifier("TestID3")
		.idType("TestType")
		.build())
	.validFrom(LocalDateTime.now())
	.validityTime("1 week")
	.build();
Pseudonym createdPseudonym3 = trustdeck.pseudonyms(createdDomain.getName())
	.create(pseudonym, false);

Pseudonym readPseudonym = trustdeck.pseudonyms(createdDomain.getName())
	.get(createdPseudonym1.getPsn());
```

Entities are scoped by project abbreviation and entity type. Build JSON payload data with Jackson and use a project entity type when creating entities:

```java
ObjectMapper mapper = new ObjectMapper();
Entity entity = new Entity();
entity.setData(mapper.valueToTree(Map.of("givenName", "Ada", "familyName", "Lovelace")));

Entity created = trustdeck.entities(projectAbbreviation, "patient").create(entity);
```

## Custom Access Tokens

Applications that acquire tokens through another OAuth flow can provide an `AccessTokenProvider` instead of configuring Keycloak password-grant credentials:

```java
AccessTokenProvider tokenProvider = () -> acquireTokenFromApplicationAuthentication();
TrustDeckClient trustdeck = new TrustDeckClient("https://trustdeck.example.com", tokenProvider);
```

The provider must return a current bearer token whenever `getAccessToken()` is called.
