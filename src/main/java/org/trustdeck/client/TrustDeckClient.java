/*
 * TrustDeck Client Library
 * Copyright 2026 Armin Müller
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

package org.trustdeck.client;

import java.util.Map;
import java.util.Set;
import java.time.Duration;

import org.springframework.http.HttpMethod;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.model.HealthStatus;
import org.trustdeck.client.service.BaseEntityTypes;
import org.trustdeck.client.service.Domains;
import org.trustdeck.client.service.Entities;
import org.trustdeck.client.service.EntityTypes;
import org.trustdeck.client.service.Permissions;
import org.trustdeck.client.service.ProjectImages;
import org.trustdeck.client.service.Projects;
import org.trustdeck.client.service.Pseudonyms;
import org.trustdeck.client.service.TrustDeckTokenService;

/**
 * Main entry point for synchronous TrustDeck API operations.
 *
 * @author Armin Müller
 */
public class TrustDeckClient {

	/** Shared HTTP transport used by all client services. */
	private final TrustDeckHttpClient http;

	/** Service for domain and domain-hierarchy operations. */
	private final Domains domains;

	/** Service for project operations. */
	private final Projects projects;

	/** Service for globally defined entity-type operations. */
	private final BaseEntityTypes baseEntityTypes;

	/** Service for permission and user operations. */
	private final Permissions permissions;

	/** Keycloak-backed token service. Null when the client uses a caller-supplied {@link AccessTokenProvider}. */
	private final TrustDeckTokenService tokenService;

	/**
	 * Creates a client that obtains tokens from the supplied Keycloak configuration.
	 *
	 * @param config Keycloak and service configuration
	 */
	public TrustDeckClient(TrustDeckClientConfig config) {
		validate(config);

		tokenService = new TrustDeckTokenService(config);
		http = new TrustDeckHttpClient(config.getServiceUrl(), tokenService,
				config.getConnectTimeout(), config.getReadTimeout());
		domains = new Domains(http);
		projects = new Projects(http);
		baseEntityTypes = new BaseEntityTypes(http);
		permissions = new Permissions(http);
	}

	/**
	 * Creates a client with caller-controlled access-token acquisition.
	 *
	 * @param serviceUrl the TrustDeck service's base URL
	 * @param tokenProvider bearer-token provider
	 */
	public TrustDeckClient(String serviceUrl, AccessTokenProvider tokenProvider) {
		this(serviceUrl, tokenProvider, TrustDeckHttpClient.DEFAULT_CONNECT_TIMEOUT,
				TrustDeckHttpClient.DEFAULT_READ_TIMEOUT);
	}

	/**
	 * Creates a client with caller-controlled token acquisition and HTTP timeouts.
	 *
	 * @param serviceUrl the TrustDeck service's base URL
	 * @param tokenProvider bearer-token provider
	 * @param connectTimeout maximum time to establish a connection
	 * @param readTimeout maximum time between response bytes
	 */
	public TrustDeckClient(String serviceUrl, AccessTokenProvider tokenProvider,
			Duration connectTimeout, Duration readTimeout) {
		tokenService = null;
		http = new TrustDeckHttpClient(serviceUrl, tokenProvider, connectTimeout, readTimeout);
		domains = new Domains(http);
		projects = new Projects(http);
		baseEntityTypes = new BaseEntityTypes(http);
		permissions = new Permissions(http);
	}

	/**
	 * Returns domain operations.
	 *
	 * @return domain service
	 */
	public Domains domains() {
		return domains;
	}

	/**
	 * Returns pseudonym operations scoped to a domain.
	 *
	 * @param domainName domain name
	 * @return scoped pseudonym service
	 */
	public Pseudonyms pseudonyms(String domainName) {
		return new Pseudonyms(http, domainName);
	}

	/**
	 * Returns project operations.
	 *
	 * @return project service
	 */
	public Projects projects() {
		return projects;
	}

	/**
	 * Returns image operations scoped to a project.
	 *
	 * @param project project abbreviation
	 * @return scoped image service
	 */
	public ProjectImages projectImages(String project) {
		return new ProjectImages(http, project);
	}

	/**
	 * Returns base entity-type operations.
	 *
	 * @return base type service
	 */
	public BaseEntityTypes baseEntityTypes() {
		return baseEntityTypes;
	}

	/**
	 * Returns entity-type operations scoped to a project.
	 *
	 * @param project project abbreviation
	 * @return scoped entity-type service
	 */
	public EntityTypes entityTypes(String project) {
		return new EntityTypes(http, project);
	}

	/**
	 * Returns entity operations scoped to a project and type.
	 *
	 * @param project project abbreviation
	 * @param type entity type name
	 * @return scoped entity service
	 */
	public Entities entities(String project, String type) {
		return new Entities(http, project, type);
	}

	/**
	 * Returns permission operations.
	 *
	 * @return permission service
	 */
	public Permissions permissions() {
		return permissions;
	}

	/**
	 * Retrieves the unauthenticated service health status.
	 *
	 * @return health status
	 */
	public HealthStatus health() {
		return http.exchange(HttpMethod.GET,
				http.uri(new String[] { "api", "health" }, Map.of()),
				null,
				HealthStatus.class,
				Set.of(200),
				false)
				.getBody();
	}

	/**
	 * Returns whether the health endpoint responds successfully.
	 *
	 * @return {@code true} when HTTP 200 is received
	 */
	public boolean ping() {
		health();

		return true;
	}

	/**
	 * Returns the Keycloak-backed token service, or {@code null} for token-provider clients.
	 *
	 * @return token service or {@code null}
	 */
	public TrustDeckTokenService getTokenService() {
		return tokenService;
	}

	/**
	 * Checks whether or not all necessary configuration information is given.
	 *
	 * @param config the configuration object
	 */
	private static void validate(TrustDeckClientConfig config) {
		if (config == null) {
			throw new IllegalArgumentException("The config must not be null.");
		}

		// Evaluate the individual parameters
		TrustDeckHttpClient.require(config.getServiceUrl(), "serviceUrl");
		TrustDeckHttpClient.require(config.getKeycloakUrl(), "keycloakUrl");
		TrustDeckHttpClient.require(config.getRealm(), "realm");
		TrustDeckHttpClient.require(config.getClientId(), "clientId");
		TrustDeckHttpClient.require(config.getClientSecret(), "clientSecret");
		TrustDeckHttpClient.require(config.getUserName(), "userName");
		TrustDeckHttpClient.require(config.getPassword(), "password");
	}
}
