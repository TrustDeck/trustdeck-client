package org.trustdeck.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.DomainTree;
import org.trustdeck.client.model.SearchResult;

/** Synchronous operations for TrustDeck domains. */
public class Domains {
	private final TrustDeckHttpClient http;
	/** Creates the domain service.
	 * @param http shared HTTP transport
	 */
	/** Creates the domain service.
	 * @param http shared HTTP transport
	 */
	public Domains(TrustDeckHttpClient http) { this.http = http; }
	/** Creates a domain; accepts HTTP 200 or 201.
	 * @param domain domain definition
	 * @return created domain
	 */
	public Domain create(Domain domain) { return object(HttpMethod.POST, new String[] { "api", "domains" }, Map.of(), domain, Set.of(200, 201)); }
	/** Creates a complete domain definition; accepts HTTP 200 or 201.
	 * @param domain domain definition
	 * @return created domain
	 */
	public Domain createComplete(Domain domain) { return object(HttpMethod.POST, new String[] { "api", "domains", "complete" }, Map.of(), domain, Set.of(200, 201)); }
	/** Deletes a domain synchronously.
	 * @param name domain name
	 * @param recursive whether children should also be deleted
	 * @return {@code true} after HTTP 204
	 */
	public boolean delete(String name, boolean recursive) { return delete(name, Boolean.valueOf(recursive)); }
	/** Deletes a domain and optionally its children.
	 * @param name domain name
	 * @param recursive recursive deletion flag
	 * @return {@code true} after HTTP 204
	 */
	public boolean delete(String name, Boolean recursive) { http.empty(HttpMethod.DELETE, http.uri(new String[] { "api", "domains" }, Map.of("name", required(name), "recursive", recursive)), null, Set.of(204), true); return true; }
	/** Gets one domain attribute.
	 * @param name domain name
	 * @param attribute attribute name
	 * @return attribute value
	 */
	public String getAttribute(String name, String attribute) { return object(HttpMethod.GET, new String[] { "api", "domains", required(name), required(attribute) }, Map.of(), null, String.class, Set.of(200)); }
	/** Gets a domain.
	 * @param name domain name
	 * @return domain
	 */
	public Domain get(String name) { return object(HttpMethod.GET, new String[] { "api", "domains", required(name) }, Map.of(), null, Set.of(200)); }
	/** Gets the backend's nested subtree without flattening it.
	 * @param name root domain name
	 * @return domain tree
	 */
	public DomainTree getSubtree(String name) { return object(HttpMethod.GET, new String[] { "api", "domains", required(name), "subtree" }, Map.of(), null, DomainTree.class, Set.of(200)); }
	/** Gets all domain trees.
	 * @return hierarchy trees
	 */
	public List<DomainTree> getHierarchy() { return list(HttpMethod.GET, new String[] { "api", "domains", "hierarchy" }, Map.of(), null, Set.of(200)); }
	/** Flattens the server's explicit subtree representation.
	 * @param name root domain name
	 * @return depth-first domain list
	 */
	public List<Domain> flattenSubtree(String name) { List<Domain> result = new ArrayList<>(); flatten(getSubtree(name), result); return result; }
	private void flatten(DomainTree tree, List<Domain> result) { if (tree == null) return; if (tree.getDomain() != null) result.add(tree.getDomain()); if (tree.getChildren() != null) tree.getChildren().forEach(child -> flatten(child, result)); }
	/** @deprecated Use {@link #getHierarchy()} and explicitly traverse its trees.
	 * @return flattened domains
	 */
	@Deprecated public List<Domain> getAll() { List<Domain> result = new ArrayList<>(); getHierarchy().forEach(tree -> flatten(tree, result)); return result; }
	/** Updates a domain and its children.
	 * @param name domain name
	 * @param domain new definition
	 * @param recursive whether children are updated
	 * @return updated domain
	 */
	public Domain updateComplete(String name, Domain domain, boolean recursive) { return object(HttpMethod.PUT, new String[] { "api", "domains", "complete" }, Map.of("name", required(name), "recursive", recursive), domain, Set.of(200)); }
	/** Updates a domain.
	 * @param name domain name
	 * @param domain new definition
	 * @return updated domain
	 */
	public Domain update(String name, Domain domain) { return object(HttpMethod.PUT, new String[] { "api", "domains" }, Map.of("name", required(name)), domain, Set.of(200)); }
	/** Replaces a domain salt.
	 * @param name domain name
	 * @param salt salt value; null is sent as an empty value
	 * @param allowEmpty whether empty salts are accepted
	 * @return updated domain
	 */
	public Domain updateSalt(String name, String salt, boolean allowEmpty) { return object(HttpMethod.PUT, new String[] { "api", "domains", required(name), "salt" }, Map.of("salt", salt == null ? "" : salt, "allowEmpty", allowEmpty), null, Set.of(200)); }
	/** Searches domains; HTTP 206 is exposed as a partial result.
	 * @param query search query
	 * @return search result
	 */
	public SearchResult<Domain> search(String query) { var response = http.exchange(HttpMethod.GET, http.uri(new String[] { "api", "domains" }, Map.of("query", required(query))), null, new ParameterizedTypeReference<List<Domain>>() {}, Set.of(200, 206), true); return new SearchResult<>(response.getBody(), response.getStatus() == 206); }
	private Domain object(HttpMethod method, String[] path, Map<String, ?> query, Object body, Set<Integer> statuses) { return object(method, path, query, body, Domain.class, statuses); }
	private <T> T object(HttpMethod method, String[] path, Map<String, ?> query, Object body, Class<T> type, Set<Integer> statuses) { return http.exchange(method, http.uri(path, query), body, type, statuses, true).getBody(); }
	private List<DomainTree> list(HttpMethod method, String[] path, Map<String, ?> query, Object body, Set<Integer> statuses) { return http.exchange(method, http.uri(path, query), body, new ParameterizedTypeReference<List<DomainTree>>() {}, statuses, true).getBody(); }
	private static String required(String value) { return TrustDeckHttpClient.require(value, "value"); }
}
