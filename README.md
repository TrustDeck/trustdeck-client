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


### Concrete Usage Example

## To run the example usage files

- AceClientExample.java
- `mvn spring-boot:run`

- AceClientSpringExample.java
- `mvn spring-boot:run -Dspring-boot.run.main-class=org.trustdeck.ace.client.AceClientSpringExample`


> **TODO:**

### How to Use in Your Application

1. Add the TrustDeck ACE Connector library as a dependency.
2. Configure your connection properties.
3. Instantiate a DomainConnector and PseudonymizationConnector using AceClientConfig.
4. Call the connector methods for Domain and Pseudonymization management.

