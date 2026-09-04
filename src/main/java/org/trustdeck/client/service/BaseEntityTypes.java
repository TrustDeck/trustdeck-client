package org.trustdeck.client.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.EntityType;
import org.trustdeck.client.model.SearchResult;

/** Operations for globally defined base entity types. */
public class BaseEntityTypes {
	private final TrustDeckHttpClient http;
	/** Creates the service.
	 * @param http shared HTTP transport
	 */
	public BaseEntityTypes(TrustDeckHttpClient http) { this.http = http; }
	/** Creates a base type.
	 * @param type type definition
	 * @return created type
	 */
	public EntityType create(EntityType type) { return call(HttpMethod.POST, new String[] { "api", "entities", "base-types" }, Map.of(), type, Set.of(200, 201)); }
	/** Gets a base type.
	 * @param name type name
	 * @return type definition
	 */
	public EntityType get(String name) { return call(HttpMethod.GET, new String[] { "api", "entities", "base-types", TrustDeckHttpClient.require(name, "entityTypeName") }, Map.of(), null, Set.of(200)); }
	/** Searches base types.
	 * @param query search query
	 * @return search result
	 */
	public SearchResult<EntityType> search(String query) { var r = http.exchange(HttpMethod.GET, http.uri(new String[] { "api", "entities", "base-types" }, Map.of("query", TrustDeckHttpClient.require(query, "query"))), null, new ParameterizedTypeReference<List<EntityType>>() {}, Set.of(200, 206), true); return new SearchResult<>(r.getBody(), r.getStatus() == 206); }
	private EntityType call(HttpMethod method, String[] path, Map<String, ?> params, Object body, Set<Integer> statuses) { return http.exchange(method, http.uri(path, params), body, EntityType.class, statuses, true).getBody(); }
}
