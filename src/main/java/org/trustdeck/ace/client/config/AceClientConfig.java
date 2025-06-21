/*
 *  * Copyright 2024 Your Organization
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
*/

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