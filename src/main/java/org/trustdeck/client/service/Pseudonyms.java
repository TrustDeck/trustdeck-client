package org.trustdeck.client.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.BatchResult;
import org.trustdeck.client.model.IdentifierItem;
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.model.PseudonymUpdate;
import org.trustdeck.client.model.SearchResult;

/** Synchronous operations for a scoped TrustDeck pseudonym domain. */
public class Pseudonyms {
	private final TrustDeckHttpClient http;
	private final String domain;
	/** Creates a domain-scoped pseudonym service.
	 * @param http shared HTTP transport
	 * @param domain domain name
	 */
	public Pseudonyms(TrustDeckHttpClient http, String domain) { this.http = http; this.domain = TrustDeckHttpClient.require(domain, "domainName"); }
	private String[] path(String... suffix) { String[] path = new String[4 + suffix.length]; path[0] = "api"; path[1] = "domains"; path[2] = domain; path[3] = "pseudonyms"; System.arraycopy(suffix, 0, path, 4, suffix.length); return path; }
	/** Creates pseudonyms in a batch; HTTP 206 is partial completion.
	 * @param values pseudonym payloads
	 * @param omitPrefix whether prefixes are omitted
	 * @return batch result
	 */
	public BatchResult<Pseudonym> createBatch(List<Pseudonym> values, boolean omitPrefix) { var r = http.exchange(HttpMethod.POST, http.uri(path("batch"), Map.of("omitPrefix", omitPrefix)), values, new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(201, 206), true); return new BatchResult<>(r.getBody(), r.getStatus(), r.getStatus() == 206); }
	/** Creates one pseudonym.
	 * @param value pseudonym payload
	 * @param omitPrefix whether the prefix is omitted
	 * @return created pseudonym
	 */
	public Pseudonym create(Pseudonym value, boolean omitPrefix) { return object(HttpMethod.POST, path(), Map.of("omitPrefix", omitPrefix), value, Set.of(200, 201)); }
	/** Creates one pseudonym using the default prefix behavior.
	 * @param value pseudonym payload
	 * @return created pseudonym
	 */
	public Pseudonym create(Pseudonym value) { return create(value, false); }
	/** Creates one pseudonym using an identifier item.
	 * @param value identifier item
	 * @param omitPrefix whether the prefix is omitted
	 * @return created pseudonym
	 */
	public Pseudonym create(IdentifierItem value, boolean omitPrefix) { return create(Pseudonym.builder().identifierItem(value).build(), omitPrefix); }
	/** Creates one pseudonym using identifier values.
	 * @param identifier identifier value
	 * @param idType identifier type
	 * @param omitPrefix whether the prefix is omitted
	 * @return created pseudonym
	 */
	public Pseudonym create(String identifier, String idType, boolean omitPrefix) { return create(IdentifierItem.builder().identifier(identifier).idType(idType).build(), omitPrefix); }
	/** Gets linked pseudonym groups.
	 * @param sourceDomain source domain
	 * @param targetDomain target domain
	 * @param identifier optional source identifier
	 * @param idType optional source identifier type
	 * @param psn optional source pseudonym
	 * @return linked groups
	 */
	public List<List<Pseudonym>> getLinkedPseudonyms(String sourceDomain, String targetDomain, String identifier, String idType, String psn) { var r = http.exchange(HttpMethod.GET, http.uri(new String[] { "api", "domains", "linked-pseudonyms" }, query(sourceDomain, targetDomain, identifier, idType, psn)), null, new ParameterizedTypeReference<List<List<Pseudonym>>>() {}, Set.of(200), true); return r.getBody(); }
	/** Gets a pseudonym by identifier.
	 * @param item identifier item
	 * @return pseudonym
	 */
	public Pseudonym get(IdentifierItem item) { return object(HttpMethod.GET, path(), Map.of("id", TrustDeckHttpClient.require(item.getIdentifier(), "identifier"), "idType", TrustDeckHttpClient.require(item.getIdType(), "idType")), null, Set.of(200)); }
	/** Gets a pseudonym by pseudonym value.
	 * @param psn pseudonym value
	 * @return pseudonym
	 */
	public Pseudonym get(String psn) { return object(HttpMethod.GET, path(), Map.of("psn", TrustDeckHttpClient.require(psn, "psn")), null, Set.of(200)); }
	/** Gets all pseudonyms in the domain.
	 * @return pseudonyms
	 */
	public List<Pseudonym> getBatch() { return http.exchange(HttpMethod.GET, http.uri(path("batch"), Map.of()), null, new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(200), true).getBody(); }
	/** Updates pseudonyms in a batch.
	 * @param updates update payloads
	 * @return updated pseudonyms
	 */
	public List<Pseudonym> updateBatch(List<PseudonymUpdate> updates) { return http.exchange(HttpMethod.PUT, http.uri(path("batch"), Map.of()), updates, new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(200), true).getBody(); }
	/** Updates one pseudonym.
	 * @param update update payload
	 * @return updated pseudonym
	 */
	public Pseudonym update(PseudonymUpdate update) { return object(HttpMethod.PUT, path(), Map.of(), update, Set.of(200)); }
	/** Updates one pseudonym and optionally regenerates its value.
	 * @param update update payload
	 * @param regenerate whether to regenerate the value
	 * @return updated pseudonym
	 */
	public Pseudonym updateComplete(PseudonymUpdate update, Boolean regenerate) { return object(HttpMethod.PUT, path("complete"), Map.of("regeneratePseudonym", regenerate), update, Set.of(200)); }
	/** Deletes all pseudonyms in a batch; HTTP 206 is partial completion.
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteBatch() { var r = http.exchange(HttpMethod.DELETE, http.uri(path("batch"), Map.of()), null, new ParameterizedTypeReference<List<Boolean>>() {}, Set.of(204, 206), true); return new BatchResult<>(r.getBody() == null ? List.of() : r.getBody(), r.getStatus(), r.getStatus() == 206); }
	/** Deletes a pseudonym by identifier after HTTP 204.
	 * @param item identifier item
	 * @return {@code true} after deletion
	 */
	public boolean delete(IdentifierItem item) { http.empty(HttpMethod.DELETE, http.uri(path(), Map.of("id", TrustDeckHttpClient.require(item.getIdentifier(), "identifier"), "idType", TrustDeckHttpClient.require(item.getIdType(), "idType"))), null, Set.of(204), true); return true; }
	/** Deletes a pseudonym by value after HTTP 204.
	 * @param psn pseudonym value
	 * @return {@code true} after deletion
	 */
	public boolean delete(String psn) { http.empty(HttpMethod.DELETE, http.uri(path(), Map.of("psn", TrustDeckHttpClient.require(psn, "psn"))), null, Set.of(204), true); return true; }
	/** Validates a pseudonym value.
	 * @param psn pseudonym value
	 * @return validation result
	 */
	public boolean validate(String psn) { return http.exchange(HttpMethod.GET, http.uri(path("validation"), Map.of("psn", TrustDeckHttpClient.require(psn, "psn"))), null, Boolean.class, Set.of(200), true).getBody(); }
	/** Searches pseudonyms and exposes HTTP 206 as partial.
	 * @param query search query
	 * @return search result
	 */
	public SearchResult<Pseudonym> search(String query) { var r = http.exchange(HttpMethod.GET, http.uri(path(), Map.of("query", TrustDeckHttpClient.require(query, "query"))), null, new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(200, 206), true); return new SearchResult<>(r.getBody(), r.getStatus() == 206); }
	private Pseudonym object(HttpMethod method, String[] path, Map<String, ?> query, Object body, Set<Integer> expected) { return http.exchange(method, http.uri(path, query), body, Pseudonym.class, expected, true).getBody(); }
	private static Map<String, Object> query(String source, String target, String identifier, String idType, String psn) { java.util.LinkedHashMap<String, Object> query = new java.util.LinkedHashMap<>(); query.put("sourceDomain", TrustDeckHttpClient.require(source, "sourceDomain")); query.put("targetDomain", TrustDeckHttpClient.require(target, "targetDomain")); query.put("sourceIdentifier", identifier); query.put("sourceIdType", idType); query.put("sourcePsn", psn); return query; }
}
