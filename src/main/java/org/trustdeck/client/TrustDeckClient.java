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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.time.Duration;

import org.springframework.http.HttpMethod;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.model.HealthStatus;
import org.trustdeck.client.service.BaseEntityTypes;
import org.trustdeck.client.service.Domains;
import org.trustdeck.client.service.Permissions;
import org.trustdeck.client.service.Projects;
import org.trustdeck.client.service.SystemOperations;
import org.trustdeck.client.service.TrustDeckTokenService;

/**
 * Main entry point for synchronous TrustDeck API operations.
 *
 * @author Armin Müller
 */
public class TrustDeckClient implements AutoCloseable {

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

	/** Service for global system statistics operations. */
	private final SystemOperations system;

	/** Keycloak-backed token service. Null when the client uses a caller-supplied {@link AccessTokenProvider}. */
	private final TrustDeckTokenService tokenService;

	/** Whether this client created the token service and therefore closes it; false for client views. */
	private final boolean ownsTokenService;

	/**
	 * Creates a client that obtains tokens from the supplied Keycloak configuration.
	 *
	 * @param config Keycloak and service configuration
	 */
	public TrustDeckClient(TrustDeckClientConfig config) {
		validate(config);

		tokenService = new TrustDeckTokenService(config);
		ownsTokenService = true;
		http = new TrustDeckHttpClient(config.getServiceUrl(), tokenService, config.getConnectTimeout(), config.getReadTimeout());
		domains = new Domains(http);
		projects = new Projects(http);
		baseEntityTypes = new BaseEntityTypes(http);
		permissions = new Permissions(http);
		system = new SystemOperations(http);
	}

	/**
	 * Creates a client with caller-controlled access-token acquisition.
	 *
	 * @param serviceUrl the TrustDeck service's base URL
	 * @param tokenProvider bearer-token provider
	 */
	public TrustDeckClient(String serviceUrl, AccessTokenProvider tokenProvider) {
		this(serviceUrl, tokenProvider, TrustDeckHttpClient.DEFAULT_CONNECT_TIMEOUT, TrustDeckHttpClient.DEFAULT_READ_TIMEOUT);
	}

	/**
	 * Creates a client with caller-controlled token acquisition and HTTP timeouts.
	 *
	 * @param serviceUrl the TrustDeck service's base URL
	 * @param tokenProvider bearer-token provider
	 * @param connectTimeout maximum time to establish a connection
	 * @param readTimeout maximum time between response bytes
	 */
	public TrustDeckClient(String serviceUrl, AccessTokenProvider tokenProvider, Duration connectTimeout, Duration readTimeout) {
		tokenService = null;
		ownsTokenService = false;
		http = new TrustDeckHttpClient(serviceUrl, tokenProvider, connectTimeout, readTimeout);
		domains = new Domains(http);
		projects = new Projects(http);
		baseEntityTypes = new BaseEntityTypes(http);
		permissions = new Permissions(http);
		system = new SystemOperations(http);
	}

	/**
	 * Creates a client view that shares the transport and token acquisition of the given client
	 * but sends the given additional headers with every request.
	 *
	 * @param source the client to derive from
	 * @param headers the additional request headers
	 */
	private TrustDeckClient(TrustDeckClient source, Map<String, String> headers) {
		tokenService = source.tokenService;
		ownsTokenService = false;
		http = source.http.withHeaders(headers);
		domains = new Domains(http);
		projects = new Projects(http);
		baseEntityTypes = new BaseEntityTypes(http);
		permissions = new Permissions(http);
		system = new SystemOperations(http);
	}

	/**
	 * Returns a client view whose requests tell TrustDeck on whose behalf they are made. This is
	 * meant for technical users (e.g. a service account) acting for an end user of another
	 * application: TrustDeck stores the value in its audit trail.
	 * <p>
	 * The value is sent in the {@value TrustDeckHttpClient#ON_BEHALF_OF_HEADER} header. It is
	 * informational only and never changes what the authenticated user is allowed to do. A
	 * {@code null} or blank value is not sent. Line breaks and other control characters are removed
	 * (backend v2.11.0 or newer stores the value).
	 * <p>
	 * The returned view shares the HTTP transport and token acquisition with this client and is
	 * cheap to create, e.g. once per incoming request. This client itself is not changed.
	 *
	 * @param onBehalfOf the end user the requests are made for, or {@code null}
	 * @return a client view sending the given value with every request
	 */
	public TrustDeckClient onBehalfOf(String onBehalfOf) {
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put(TrustDeckHttpClient.ON_BEHALF_OF_HEADER, onBehalfOf);

		return new TrustDeckClient(this, headers);
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
	 * Creates an immutable project scope without making an HTTP request.
	 *
	 * @param projectAbbreviation project abbreviation
	 * @return immutable project scope
	 */
	public ProjectScope project(String projectAbbreviation) {
		return new ProjectScope(http, projects, domains, permissions, projectAbbreviation);
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
	 * Returns base entity-type operations.
	 *
	 * @return base type service
	 */
	public BaseEntityTypes baseEntityTypes() {
		return baseEntityTypes;
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
	 * Returns global system operations without making an HTTP request.
	 *
	 * @return global system operations
	 */
	public SystemOperations system() {
		return system;
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
	 * Releases the HTTP client this client uses for Keycloak. Afterwards, requests that need a new
	 * token fail. Clients created with an {@link AccessTokenProvider} hold no such resources, and
	 * closing a view returned by {@link #onBehalfOf(String)} has no effect; close the original
	 * client instead. Calling this method more than once has no effect.
	 */
	@Override
	public void close() {
		if (ownsTokenService) {
			tokenService.close();
		}
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

		// User credentials are only needed for the password grant
		if (!config.usesClientCredentials()) {
			TrustDeckHttpClient.require(config.getUserName(), "userName");
			TrustDeckHttpClient.require(config.getPassword(), "password");
		}
	}
}
