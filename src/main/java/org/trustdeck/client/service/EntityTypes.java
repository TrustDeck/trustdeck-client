package org.trustdeck.client.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.EntityType;
import org.trustdeck.client.model.SearchResult;

/** Operations for entity types scoped to one project. */
public class EntityTypes {
	private final TrustDeckHttpClient http; private final String project;
	/** Creates the service.
	 * @param http shared HTTP transport
	 * @param project project abbreviation
	 */
	public EntityTypes(TrustDeckHttpClient http, String project) { this.http = http; this.project = TrustDeckHttpClient.require(project, "projectAbbreviation"); }
	/** Creates an entity type.
	 * @param type type definition
	 * @return created type
	 */
	public EntityType create(EntityType type) { return call(HttpMethod.POST, config(), Map.of(), type, Set.of(200, 201)); }
	/** Gets an entity type.
	 * @param type type name
	 * @return type definition
	 */
	public EntityType get(String type) { return call(HttpMethod.GET, config(TrustDeckHttpClient.require(type, "entityTypeName")), Map.of(), null, Set.of(200)); }
	/** Updates an entity type.
	 * @param type type name
	 * @param value type definition
	 * @return updated type
	 */
	public EntityType update(String type, EntityType value) { return call(HttpMethod.PUT, config(TrustDeckHttpClient.require(type, "entityTypeName")), Map.of(), value, Set.of(200)); }
	/** Deletes an entity type.
	 * @param type type name
	 * @return {@code true} when accepted
	 */
	public boolean delete(String type) { http.empty(HttpMethod.DELETE, http.uri(config(TrustDeckHttpClient.require(type, "entityTypeName")), Map.of()), null, Set.of(204), true); return true; }
	/** Searches entity types.
	 * @param query search query
	 * @return search result
	 */
	public SearchResult<EntityType> search(String query) { var r = http.exchange(HttpMethod.GET, http.uri(new String[] { "api", "projects", project, "entities" }, Map.of("query", TrustDeckHttpClient.require(query, "query"))), null, new ParameterizedTypeReference<List<EntityType>>() {}, Set.of(200, 206), true); return new SearchResult<>(r.getBody(), r.getStatus() == 206); }
	private String[] config(String... suffix) { String[] path = new String[5 + suffix.length]; String[] prefix = { "api", "projects", project, "entities", "config" }; System.arraycopy(prefix, 0, path, 0, prefix.length); System.arraycopy(suffix, 0, path, prefix.length, suffix.length); return path; }
	private EntityType call(HttpMethod method, String[] path, Map<String, ?> params, Object body, Set<Integer> statuses) { return http.exchange(method, http.uri(path, params), body, EntityType.class, statuses, true).getBody(); }
}
