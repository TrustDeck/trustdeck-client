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
TrustDeckClient client = new TrustDeckClient(config);
```

## Running the Example

After setting the connection variables, run the complete example with:

```bash
mvn clean compile exec:java
```

The example performs real requests against the configured backend. It creates uniquely named projects, images, base and project-scoped entity types, domains, entities, and pseudonyms, reads the created resources, validates the pseudonym, and deletes the resources that support deletion in reverse dependency order.

Base entity types currently have no delete operation in the TrustDeck API. The generated base entity type therefore remains after the example finishes and must be removed through an administrative backend operation if necessary.

## Entities

Entities are scoped by project abbreviation and entity type. Build JSON payload data with Jackson.

```java
ObjectMapper mapper = new ObjectMapper();
Entity entity = new Entity();
entity.setData(mapper.valueToTree(Map.of("givenName", "Ada", "familyName", "Lovelace")));

Entity created = client.entities("research", "patient")
	.create(entity, RecordLinkageResolutionStrategy.CREATE_ORIGINAL);
```

The record-linkage strategy is imported from the nested model enum:

```java
import org.trustdeck.client.model.RecordLinkageCandidate.RecordLinkageResolutionStrategy;
```

Use `client.domains()`, `client.pseudonyms(domainName)`, `client.projects()`, `client.projectImages(project)`, `client.baseEntityTypes()`, `client.entityTypes(project)`, and `client.permissions()` for their corresponding resources. `client.health()` performs unauthenticated health lookup; `ping()` delegates to it.

## Custom Access Tokens

Applications that acquire tokens through another OAuth flow can provide an `AccessTokenProvider` instead of configuring Keycloak password-grant credentials:

```java
AccessTokenProvider tokenProvider = () -> acquireTokenFromApplicationAuthentication();
TrustDeckClient client = new TrustDeckClient("https://trustdeck.example.com", tokenProvider);
```

The provider must return a current bearer token whenever `getAccessToken()` is called.

