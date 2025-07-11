package org.trustdeck.ace.client.config;

import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.trustdeck.ace.client.service.DomainConnector;
import org.trustdeck.ace.client.service.PersonConnector;
import org.trustdeck.ace.client.service.PseudonymConnector;

/**
 * Configuration class for creating ACE client connector instances using provided properties.
 */
@Configuration
@EnableConfigurationProperties(AceClientProperties.class)
@Getter
public class AceClientConfig {

    private final AceClientProperties properties;

    /**
     * Constructor to initialize with AceClientProperties.
     *
     * @param properties The configuration properties
     */
    public AceClientConfig(AceClientProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates a new instance of PseudonymizationConnector.
     *
     * @return a new instance of PseudonymizationConnector
     */
    public PseudonymConnector createPseudonymizationConnector() {
        return new PseudonymConnector(
                properties.getServiceUrl(),
                properties.getKeycloakUrl(),
                properties.getRealm(),
                properties.getClientId(),
                properties.getClientSecret(),
                properties.getUserName(),
                properties.getPassword()

        );
    }

    /**
     * Creates a new instance of DomainConnector.
     *
     * @return a new instance of DomainConnector
     */
    public DomainConnector createDomainConnector() {
        return new DomainConnector(
                properties.getServiceUrl(),
                properties.getKeycloakUrl(),
                properties.getRealm(),
                properties.getClientId(),
                properties.getClientSecret(),
                properties.getUserName(),
                properties.getPassword()

        );
    }


        public PersonConnector createPersonConnector() {
            return new PersonConnector(
                    properties.getServiceUrl(),
                    properties.getKeycloakUrl(),
                    properties.getRealm(),
                    properties.getClientId(),
                    properties.getClientSecret(),
                    properties.getUserName(),
                    properties.getPassword()

            );

}
}