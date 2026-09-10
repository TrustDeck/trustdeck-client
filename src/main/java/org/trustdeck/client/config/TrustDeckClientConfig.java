/*
 * TrustDeck Client Library
 * Copyright 2025 Armin Müller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustdeck.client.config;

import java.time.Duration;

import lombok.Builder;
import lombok.Getter;
import org.trustdeck.client.TrustDeckHttpClient;

/**
 * Configuration builder class as an alternative way to configure the client.
 *
 * @author Armin Müller
 */
@Builder
@Getter
public class TrustDeckClientConfig {

	/**
	 * Creates empty configuration for builder-generated population.
	 */
	public TrustDeckClientConfig() {
		// Nothing to do
	}

	/**
	 * Creates configuration with all authentication and service values.
	 *
	 * @param serviceUrl TrustDeck service URL
	 * @param keycloakUrl Keycloak URL
	 * @param realm Keycloak realm
	 * @param clientId Keycloak client ID
	 * @param clientSecret Keycloak client secret
	 * @param userName user name
	 * @param password password
	 */
	public TrustDeckClientConfig(String serviceUrl, String keycloakUrl, String realm, String clientId, String clientSecret,
			String userName, String password) {
		this(serviceUrl, keycloakUrl, realm, clientId, clientSecret, userName, password,
				TrustDeckHttpClient.DEFAULT_CONNECT_TIMEOUT, TrustDeckHttpClient.DEFAULT_READ_TIMEOUT);
	}

	/**
	 * Creates configuration with authentication, service, and transport values.
	 *
	 * @param serviceUrl TrustDeck service URL
	 * @param keycloakUrl Keycloak URL
	 * @param realm Keycloak realm
	 * @param clientId Keycloak client ID
	 * @param clientSecret Keycloak client secret
	 * @param userName user name
	 * @param password password
	 * @param connectTimeout maximum time to establish an HTTP connection
	 * @param readTimeout maximum time between response bytes
	 */
	public TrustDeckClientConfig(String serviceUrl, String keycloakUrl, String realm, String clientId, String clientSecret,
			String userName, String password, Duration connectTimeout, Duration readTimeout) {
		this.serviceUrl = serviceUrl;
		this.keycloakUrl = keycloakUrl;
		this.realm = realm;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.userName = userName;
		this.password = password;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}

	/** The base URL of the TrustDeck instance to work with. */
	private String serviceUrl;

	/** The URL of the Keycloak authentication server for this TrustDeck instance. */
	private String keycloakUrl;

	/** The name of the Keycloak realm. */
	private String realm;

	/** The client ID for authenticating against Keycloak. */
	private String clientId;

	/** The client secret for authenticating against Keycloak. */
	private String clientSecret;

	/** The user name for authenticating against Keycloak. */
	private String userName;

	/** The user's password for authenticating against Keycloak. */
	private String password;

	/** Maximum time to establish an HTTP connection. */
	@Builder.Default
	private Duration connectTimeout = TrustDeckHttpClient.DEFAULT_CONNECT_TIMEOUT;

	/** Maximum time between response bytes. */
	@Builder.Default
	private Duration readTimeout = TrustDeckHttpClient.DEFAULT_READ_TIMEOUT;
}
