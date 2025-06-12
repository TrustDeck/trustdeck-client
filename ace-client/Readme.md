# TrustDeck ACE Client Library 
A Client library that provides access to the APIs of the TrustDeck ACE service.

> **Note:** This project is currently under active development. APIs and features may change.

## Overview

The TrustDeck ACE Client Library serves as a client-side interface to the TrustDeck ACE service, providing endpoints for:

- Domain management (create, read, update, delete)
- Pseudonym operations (creation, retrieval, update, deletion)

## Requirements

- Java 17 or later
- Maven 3.6+
- Keycloak authentication server
- TrustDeck ACE service

## Architecture
-  Connectors: DomainConnector, PseudonymizationConnector
-  DTOs: DomainDto, PseudonymDto
-  Configuration: AceClientConfig, AceClientProperties
-  Utility: AceClientUtil (handles headers and token management
- 
## Getting Started

### Configuration

Instantiate connectors using AceClientConfig (Local):

    `AceClientProperties props = new AceClientProperties(
        "http://localhost:8080",
        "http://localhost:8081",
        "development",
        "ace",
        "your-client-secret",
        "test",
        "your-password"
        );
     DomainConnector domainConnector = AceClientConfig.createDomainConnector(props);
     PseudonymizationConnector pseudonymConnector = AceClientConfig.createPseudonymizationConnector(props);`


### Testing the Library
Integration tests are provided in ace-client/src/test/java/org/trustdeck/ace/client/AceClientConfigTest.java.
These tests demonstrate real usage of the DomainConnector methods, including authentication and token management via AceClientUtil.

Example: Integration Test for DomainConnector

### Testing the API
    @BeforeEach
    void setUp() {
    AceClientProperties props = new AceClientProperties(
    "http://localhost:8080",
    "http://localhost:8081",
    "development",
    "ace",
    "your-client-secret",
    "test",
    "your-password"
    );
    domainConnector = AceClientConfig.createDomainConnector(props);
    }

    @Test
    void testGetDomain() {
    DomainDto domain = domainConnector.getDomain("TestStudie-KI");
    assertNotNull(domain);
    }





## Main Endpoints from the ACE service

> **Note:** The connector library provides convenient methods for interacting with these APIs.
TrustDeck ACE Connector handles authentication and headers internally using AceClientUtil.

### Domain Management endpoints from Trustdeck Ace

- `GET /api/pseudonymization/domain?name={domainName}` — Get a specific domain
- `GET /api/pseudonymization/experimental/domains/hierarchy` — Get all domains (hierarchy)
- `GET /api/pseudonymization/domains/{domainName}/{attributeName}` — Get a specific attribute of a domain
- `POST /api/pseudonymization/domain` — Create a new domain (reduced attributes)
- `POST /api/pseudonymization/domain/complete-`— Create a new domain (all attributes)
- `PUT /api/pseudonymization/domain?name={domainName}` — Update a domain (reduced attributes)
- `PUT /api/pseudonymization/domain/complete?name={domainName}&recursive={true|false}` — Update a domain (all attributes, optionally recursive)
- `DELETE /api/pseudonymization/domain?name={domainName}&recursive={true|false}` — Delete a domain (optionally recursive)
- `PUT /api/pseudonymization/domains/{domainName}/salt?salt={newSalt}&allowEmpty={true|false}`~~~~ — Update the salt of a domain
- 
### Pseudonymization endpoints from Trustdeck Ace

- `POST /api/pseudonymization/domains/{domainName}/pseudonyms?omitPrefix={true|false}` — Create a batch of pseudonyms
- `POST /api/pseudonymization/domains/{domainName}/pseudonym?omitPrefix={true|false}` — Create a single pseudonym
- `GET /api/pseudonymization/domains/{domainName}/pseudonyms` — Get all pseudonyms in a domain
- `GET /api/pseudonymization/domains/{domainName}/pseudonym?id={identifier}&idType={idType}` — Get pseudonym by identifier
- `GET /api/pseudonymization/domains/{domainName}/pseudonym?psn={psn}` — Get pseudonym by pseudonym value
- `PUT /api/pseudonymization/domains/{domainName}/pseudonyms` — Update a batch of pseudonyms
- `PUT /api/pseudonymization/domains/{domainName}/pseudonym?id={identifier}&idType={idType}` — Update pseudonym by identifier
- `PUT /api/pseudonymization/domains/{domainName}/pseudonym?psn={psn}` — Update pseudonym by pseudonym value
- `PUT /api/pseudonymization/domains/{domainName}/pseudonym/complete?id={identifier}&idType={idType} `— Update complete pseudonym by identifier
- `PUT /api/pseudonymization/domains/{domainName}/pseudonym/complete?psn={psn`} — Update complete pseudonym by pseudonym value
- `DELETE /api/pseudonymization/domains/{domainName}/pseudonyms` — Delete all pseudonyms in a domain
- `DELETE /api/pseudonymization/domains/{domainName}/pseudonym?id={identifier}&idType={idType}` — Delete pseudonym by identifier
- `DELETE /api/pseudonymization/domains/{domainName}/pseudonym?psn={psn}` — Delete pseudonym by pseudonym value
- `GET /api/pseudonymization/domains/linked-pseudonyms?sourceDomain={sourceDomain}&targetDomain={targetDomain}&sourceIdentifier={sourceIdentifier}&sourceIdType={sourceIdType}&sourcePsn={sourcePsn} `— Get linked pseudonyms between domains
- `GET /api/pseudonymization/domains/{domainName}/pseudonym/validation?psn={psn}` — Validate a pseudonym value



### How to Use in Your Application

1. Add the TrustDeck ACE Connector library as a dependency.
2. Configure your connection properties.
3. Instantiate a DomainConnector and PseudonymizationConnector using AceClientConfig.
4. Call the connector methods for Domain and Pseudonymization management.