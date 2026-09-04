package org.trustdeck.client.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.exception.RecordLinkageConflictException;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.Entity;
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.model.RecordLinkageCandidate;
import org.trustdeck.client.model.RecordLinkageCandidate.RecordLinkageResolution;
import org.trustdeck.client.model.SearchResult;

/** Operations for entities scoped to a project and type. */
public class Entities {
	private final TrustDeckHttpClient http; private final String project; private final String type;
	/** Creates a project/type-scoped entity service.
	 * @param http shared HTTP transport
	 * @param project project abbreviation
	 * @param type entity type name
	 */
	public Entities(TrustDeckHttpClient http, String project, String type) { this.http = http; this.project = TrustDeckHttpClient.require(project, "projectAbbreviation"); this.type = TrustDeckHttpClient.require(type, "entityTypeName"); }
	/** Creates an entity.
	 * @param entity entity payload
	 * @return created entity
	 */
	/** Creates an entity without a record-linkage resolution.
	 * @param entity entity payload
	 * @return created entity
	 */
	public Entity create(Entity entity) { return create(entity, null); }
	/** Creates an entity with optional record-linkage resolution.
	 * @param entity entity payload
	 * @param resolution optional record-linkage decision
	 * @return created entity
	 * @throws RecordLinkageConflictException when resolution is required
	 */
	public Entity create(Entity entity, RecordLinkageResolution resolution) { java.util.Map<String, Object> q = new java.util.LinkedHashMap<>(); q.put("recordLinkageResolution", resolution); try { return http.exchange(HttpMethod.POST, http.uri(path(), q), entity, Entity.class, Set.of(200, 201), true).getBody(); } catch (TrustDeckResponseException e) { if (e.getStatusCode() != 409) throw e; try { List<RecordLinkageCandidate> candidates = http.mapper().readValue(e.getRawBody(), http.mapper().getTypeFactory().constructCollectionType(List.class, RecordLinkageCandidate.class)); throw new RecordLinkageConflictException(e.getResponseStatusCode(), e.getStatusInfo(), e.getRawBody(), e.getContentType(), candidates); } catch (RecordLinkageConflictException conflict) { throw conflict; } catch (Exception parseFailure) { throw e; } } }
	/** Gets an entity.
	 * @param id entity identifier
	 * @return entity
	 */
	public Entity get(UUID id) { return call(HttpMethod.GET, path(id), Map.of(), null, Set.of(200)); }
	/** Updates an entity.
	 * @param id entity identifier
	 * @param entity replacement payload
	 * @return updated entity
	 */
	public Entity update(UUID id, Entity entity) { return call(HttpMethod.PUT, path(id), Map.of(), entity, Set.of(200)); }
	/** Deletes an entity after HTTP 204.
	 * @param id entity identifier
	 * @return {@code true} after successful deletion
	 */
	public boolean delete(UUID id) { http.empty(HttpMethod.DELETE, http.uri(path(id), Map.of()), null, Set.of(204), true); return true; }
	/** Searches entities and exposes HTTP 206 as partial.
	 * @param query search query
	 * @return search result
	 */
	public SearchResult<Entity> search(String query) { var r = http.exchange(HttpMethod.GET, http.uri(path(), Map.of("query", TrustDeckHttpClient.require(query, "query"))), null, new ParameterizedTypeReference<List<Entity>>() {}, Set.of(200, 206), true); return new SearchResult<>(r.getBody(), r.getStatus() == 206); }
	/** Lists pseudonyms associated with an entity.
	 * @param id entity identifier
	 * @return search result
	 */
	public SearchResult<Pseudonym> getPseudonyms(UUID id) { var r = http.exchange(HttpMethod.GET, http.uri(path(id, "pseudonyms"), Map.of()), null, new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(200, 206), true); return new SearchResult<>(r.getBody(), r.getStatus() == 206); }
	/** Finds record-linkage candidates for an entity.
	 * @param entity entity payload
	 * @return candidate list, empty for HTTP 204
	 */
	public List<RecordLinkageCandidate> findRecordLinkageCandidates(Entity entity) { var r = http.exchange(HttpMethod.POST, http.uri(path("record-linkage"), Map.of()), entity, new ParameterizedTypeReference<List<RecordLinkageCandidate>>() {}, Set.of(200, 204), true); return r.getBody() == null ? List.of() : r.getBody(); }
	private Entity call(HttpMethod method, String[] path, Map<String, ?> query, Entity body, Set<Integer> expected) { return http.exchange(method, http.uri(path, query), body, Entity.class, expected, true).getBody(); }
	private String[] path(Object... suffix) { String[] path = new String[5 + suffix.length]; String[] prefix = { "api", "projects", project, "entities", type }; System.arraycopy(prefix, 0, path, 0, prefix.length); for (int i = 0; i < suffix.length; i++) path[i + prefix.length] = String.valueOf(suffix[i]); return path; }
}
