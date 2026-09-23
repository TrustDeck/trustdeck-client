# TrustDeck Client Library

Synchronous Java access to TrustDeck domains, pseudonyms, projects, project images, entity types, generic entities, permissions, users, and health.

## Version 2.0.0 API

Project-scoped resources are reached through an immutable `ProjectScope`. Calling
`client.project("abbr")`, or navigating from that scope, makes no HTTP request and
does not acquire a token. A scope keeps its original abbreviation after a project
rename. `scope.get("other")` is the approved convenience lookup and does not
rebind the scope.

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

The active Maven dependency is:

```xml
<dependency>
    <groupId>org.trustdeck</groupId>
    <artifactId>client</artifactId>
    <version>2.0.0</version>
</dependency>
```

Released versions are obtained from Maven Central and do not require
repository-specific credentials.

Client 2.0.0 supports the typed statistics responses introduced and completed
in backend v2.3.0. The client and backend use independent version numbers.

```java
ProjectStatistics projectStatistics = trustdeck.project("research").getStatistics();
SystemStatistics systemStatistics = trustdeck.system().getStatistics();
```

Project statistics requires `project:statistics` on the selected project.
System statistics requires the global `system:statistics` permission. These
permissions are enforced by the backend.

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
ProjectScope projectScope = trustdeck.project(projectAbbreviation);

Domain domain = Domain.builder()
	.name("research-domain")
	.prefix("RD")
	.build();
Domain createdDomain = projectScope.domains().create(domain);

// Create a pseudonym from an identifier item
IdentifierItem identifierItem = IdentifierItem.builder()
	.identifier("TestID1")
	.idType("TestType")
	.build();
Pseudonym createdPseudonym1 = projectScope.pseudonyms(createdDomain.getName())
	.create(identifierItem, false);

// Create a pseudonym directly from an identifier and identifier type
Pseudonym createdPseudonym2 = projectScope.pseudonyms(createdDomain.getName())
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
Pseudonym createdPseudonym3 = projectScope.pseudonyms(createdDomain.getName())
	.create(pseudonym, false);

Pseudonym readPseudonym = projectScope.pseudonyms(createdDomain.getName())
	.get(createdPseudonym1.getPsn());
```

Entities are scoped by project and entity type. Build JSON payload data with Jackson and use a project entity type when creating entities:

```java
ObjectMapper mapper = new ObjectMapper();
Entity entity = new Entity();
entity.setData(mapper.valueToTree(Map.of("givenName", "Ada", "familyName", "Lovelace")));

Entity created = projectScope.entities("patient").create(entity);
```

Project domains expose `getAll()` as `List<ProjectDomain>` summaries from the
project route. Their `search(query)` method is deliberately an unfiltered alias
of global `client.domains().search(query)` and adds no project query parameter.
For domain create and update calls, a missing `projectAbbreviation` is filled in
on a copied payload. Blank or conflicting values are rejected locally;
case-insensitive matching preserves the caller's supplied value. The copy does
not mutate the caller or nested algorithm data. This context does not prove or
enforce domain ownership; backend authorization remains authoritative.

## Custom Access Tokens

Applications that acquire tokens through another OAuth flow can provide an `AccessTokenProvider` instead of configuring Keycloak password-grant credentials:

```java
AccessTokenProvider tokenProvider = () -> acquireTokenFromApplicationAuthentication();
TrustDeckClient trustdeck = new TrustDeckClient("https://trustdeck.example.com", tokenProvider);
```

The provider must return a current bearer token whenever `getAccessToken()` is called.
