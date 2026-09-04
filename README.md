# TrustDeck Client Library

Synchronous Java access to TrustDeck domains, pseudonyms, projects, project images, entity types, generic entities, permissions, users, and health.

## Configuration

Create a `TrustDeckClient` with password-grant settings, preferably loaded from environment variables or a secrets manager. Applications with another OAuth flow may provide an `AccessTokenProvider` instead.

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

## Entities

Entities are scoped by project abbreviation and entity type. Build JSON payload data with Jackson.

```java
ObjectMapper mapper = new ObjectMapper();
Entity entity = new Entity();
entity.setData(mapper.valueToTree(Map.of("givenName", "Ada", "familyName", "Lovelace")));

Entity created = client.entities("research", "patient")
	.create(entity, RecordLinkageResolution.CREATE_ORIGINAL);
```

Use `client.domains()`, `client.pseudonyms(domainName)`, `client.projects()`, `client.projectImages(project)`, `client.baseEntityTypes()`, `client.entityTypes(project)`, and `client.permissions()` for their corresponding resources. `client.health()` performs unauthenticated health lookup; `ping()` delegates to it.

## Migration From 0.6.0-BETA

- Health is now `/api/health`; `/api/ping` is removed.
- The obsolete person API is removed. Migrate callers to `entities(projectAbbreviation, entityTypeName)` and use generic entities.
- Domains use nested `Domain.algorithm` rather than flattened algorithm fields.
- Pseudonym updates use `PseudonymUpdate` rather than a `Pseudonym` request body.
- Searches and batch requests expose 206 truncation or partial completion through `SearchResult` and `BatchResult`.
- Failures retain HTTP status, parsed backend status information, content type, location, and a bounded raw response body in `TrustDeckResponseException`.
- `getSubtree` returns the backend `DomainTree` shape. Single pseudonym deletion is a 204-only boolean operation.

## Coverage

`ENDPOINTS.md` records the 69 reviewed backend business endpoints at backend commit `54bf747d0bfe04dfc0a816b4e49c2944abd71e60`, including the maintenance and infrastructure exclusions.
