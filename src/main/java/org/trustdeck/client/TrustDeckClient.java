package org.trustdeck.client;

import java.util.Map;
import java.util.Set;

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

/** Main entry point for synchronous TrustDeck API operations. */
public class TrustDeckClient {
	private final TrustDeckHttpClient http;
	private final Domains domains;
	private final Projects projects;
	private final BaseEntityTypes baseEntityTypes;
	private final Permissions permissions;
	private final TrustDeckTokenService tokenService;
	/** Creates a client that obtains tokens from the supplied Keycloak configuration.
	 * @param config Keycloak and service configuration
	 */
	public TrustDeckClient(TrustDeckClientConfig config) { validate(config); tokenService = new TrustDeckTokenService(config); http = new TrustDeckHttpClient(config.getServiceUrl(), tokenService); domains = new Domains(http); projects = new Projects(http); baseEntityTypes = new BaseEntityTypes(http); permissions = new Permissions(http); }
	/** Creates a client with caller-controlled access-token acquisition.
	 * @param serviceUrl TrustDeck service base URL
	 * @param tokenProvider bearer-token provider
	 */
	public TrustDeckClient(String serviceUrl, AccessTokenProvider tokenProvider) { tokenService = null; http = new TrustDeckHttpClient(serviceUrl, tokenProvider); domains = new Domains(http); projects = new Projects(http); baseEntityTypes = new BaseEntityTypes(http); permissions = new Permissions(http); }
	/** Returns domain operations.
	 * @return domain service
	 */
	public Domains domains() { return domains; }
	/** Returns pseudonym operations scoped to a domain.
	 * @param domainName domain name
	 * @return scoped pseudonym service
	 */
	public Pseudonyms pseudonyms(String domainName) { return new Pseudonyms(http, domainName); }
	/** Returns project operations.
	 * @return project service
	 */
	public Projects projects() { return projects; }
	/** Returns image operations scoped to a project.
	 * @param project project abbreviation
	 * @return scoped image service
	 */
	public ProjectImages projectImages(String project) { return new ProjectImages(http, project); }
	/** Returns base entity-type operations.
	 * @return base type service
	 */
	public BaseEntityTypes baseEntityTypes() { return baseEntityTypes; }
	/** Returns entity-type operations scoped to a project.
	 * @param project project abbreviation
	 * @return scoped entity-type service
	 */
	public EntityTypes entityTypes(String project) { return new EntityTypes(http, project); }
	/** Returns entity operations scoped to a project and type.
	 * @param project project abbreviation
	 * @param type entity type name
	 * @return scoped entity service
	 */
	public Entities entities(String project, String type) { return new Entities(http, project, type); }
	/** Returns permission operations.
	 * @return permission service
	 */
	public Permissions permissions() { return permissions; }
	/** Retrieves the unauthenticated service health status.
	 * @return health status
	 */
	public HealthStatus health() { return http.exchange(HttpMethod.GET, http.uri(new String[] { "api", "health" }, Map.of()), null, HealthStatus.class, Set.of(200), false).getBody(); }
	/** Returns whether the health endpoint responds successfully.
	 * @return {@code true} when HTTP 200 is received
	 */
	public boolean ping() { health(); return true; }
	/** Returns the Keycloak-backed token service, or {@code null} for token-provider clients.
	 * @return token service or {@code null}
	 */
	public TrustDeckTokenService getTokenService() { return tokenService; }
	private static void validate(TrustDeckClientConfig config) { if (config == null) throw new IllegalArgumentException("config must not be null."); TrustDeckHttpClient.require(config.getServiceUrl(), "serviceUrl"); TrustDeckHttpClient.require(config.getKeycloakUrl(), "keycloakUrl"); TrustDeckHttpClient.require(config.getRealm(), "realm"); TrustDeckHttpClient.require(config.getClientId(), "clientId"); TrustDeckHttpClient.require(config.getClientSecret(), "clientSecret"); TrustDeckHttpClient.require(config.getUserName(), "userName"); TrustDeckHttpClient.require(config.getPassword(), "password"); }
}
