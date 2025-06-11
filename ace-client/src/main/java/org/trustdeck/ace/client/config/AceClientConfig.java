package org.trustdeck.ace.client.config;

import org.trustdeck.ace.client.service.DomainConnector;
import org.trustdeck.ace.client.service.PseudonymizationConnector;

/**
 * Factory class to create ACE client connector instances.
 * Use this class to instantiate connectors with pre-configured properties.
 * Example: {@code AceClientConfig.createDomainConnector(props);}
 */
public class AceClientConfig {

    /**
     * Creates a new instance of PseudonymizationConnector.
     *
     * @param props The pseudonymization configuration properties
     * @return a new instance of PseudonymizationConnector
     */
    public static PseudonymizationConnector createPseudonymizationConnector(AceClientProperties props) {
        return new PseudonymizationConnector(
                props.getServiceUrl(),
                props.getKeycloakUrl(),
                props.getRealm(),
                props.getClientId(),
                props.getClientSecret(),
                props.getUserName(),
                props.getPassword()
        );
    }

    /**
     * Creates a new instance of DomainConnector.
     *
     * @param props The pseudonymization configuration properties
     * @return a new instance of DomainConnector
     */
    public static DomainConnector createDomainConnector(AceClientProperties props) {
        return new DomainConnector(
                props.getServiceUrl(),
                props.getKeycloakUrl(),
                props.getRealm(),
                props.getClientId(),
                props.getClientSecret(),
                props.getUserName(),
                props.getPassword()
        );
    }
}