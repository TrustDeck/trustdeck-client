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

/**
 * Synchronous operations for TrustDeck domains.
 *
 * @author Armin Müller
 */
public class Domains {

	/** Shared HTTP transport object. */
	private final TrustDeckHttpClient http;

	/**
	 * Creates the domain service.
	 * 
	 * @param http shared HTTP transport
	 */
	public Domains(TrustDeckHttpClient http) {
		this.http = http;
	}

	/**
	 * Creates a domain; accepts HTTP 200 or 201.
	 * 
	 * @param domain the domain definition
	 * @return created domain
	 */
	public Domain create(Domain domain) {
		return request(HttpMethod.POST, new String[] {"api", "domains"}, Map.of(), domain, Set.of(200, 201));
	}
	
	/**
	 * Creates a complete domain definition; accepts HTTP 200 or 201.
	 * 
	 * @param domain the domain definition
	 * @return created domain
	 */
	public Domain createComplete(Domain domain) {
		return request(HttpMethod.POST, new String[] {"api", "domains", "complete"}, Map.of(), domain, Set.of(200, 201));
	}

	/**
	 * Deletes a domain and optionally its children.
	 * 
	 * @param domainName the domain name
	 * @param recursive recursive deletion flag
	 * @return {@code true} after HTTP 204
	 */
	public boolean delete(String domainName, Boolean recursive) {
		http.empty(HttpMethod.DELETE,
				http.uri(new String[] {"api", "domains"}, Map.of("name", required(domainName), "recursive", recursive)),
				null, Set.of(204), true);

		return true;
	}

	/**
	 * Gets one domain attribute.
	 * 
	 * @param domainName domain name
	 * @param attributeName attribute name
	 * @return attribute value
	 */
	public String getAttribute(String domainName, String attributeName) {
		return request(HttpMethod.GET, new String[] {"api", "domains", required(domainName), required(attributeName)}, 
				Map.of(), null, String.class, Set.of(200));
	}

	/**
	 * Gets a domain.
	 * 
	 * @param domainName the domain name
	 * @return domain
	 */
	public Domain get(String domainName) {
		return request(HttpMethod.GET, new String[] {"api", "domains", required(domainName)}, Map.of(), null, Set.of(200));
	}

	/**
	 * Gets the backend's nested subtree without flattening it.
	 * 
	 * @param domainName root domain name
	 * @return domain tree
	 */
	public DomainTree getSubtree(String domainName) {
		return request(HttpMethod.GET, new String[] {"api", "domains", required(domainName), "subtree"}, Map.of(), null,
				DomainTree.class, Set.of(200));
	}

	/**
	 * Gets all domain trees.
	 * 
	 * @return hierarchy trees
	 */
	public List<DomainTree> getHierarchy() {
		return list(HttpMethod.GET, new String[] {"api", "domains", "hierarchy"}, Map.of(), null, Set.of(200));
	}

	/**
	 * Flattens the server's explicit subtree representation.
	 * 
	 * @param domainName root domain name
	 * @return depth-first domain list
	 */
	public List<Domain> flattenSubtree(String domainName) {
		List<Domain> result = new ArrayList<>();
		flatten(getSubtree(domainName), result);

		return result;
	}

	/**
	 * Flattens one tree depth-first into the supplied result list.
	 * 
	 * @param tree tree to flatten
	 * @param result destination list
	 */
	private void flatten(DomainTree tree, List<Domain> result) {
		if (tree == null) {
			return;
		}

		if (tree.getDomain() != null) {
			result.add(tree.getDomain());
		}

		if (tree.getChildren() != null) {
			tree.getChildren().forEach(child -> flatten(child, result));
		}
	}

	/**
	 * Updates a domain and its children.
	 * 
	 * @param domainName domain name
	 * @param updatedDomain new definition
	 * @param recursive whether children are updated
	 * @return updated domain
	 */
	public Domain updateComplete(String domainName, Domain updatedDomain, boolean recursive) {
		return request(HttpMethod.PUT, new String[] {"api", "domains", "complete"},
				Map.of("name", required(domainName), "recursive", recursive), updatedDomain, Set.of(200));
	}

	/**
	 * Updates a domain.
	 * 
	 * @param domainName domain name
	 * @param updatedDomain new definition
	 * @return updated domain
	 */
	public Domain update(String domainName, Domain updatedDomain) {
		return request(HttpMethod.PUT, new String[] {"api", "domains"}, Map.of("name", required(domainName)), updatedDomain, Set.of(200));
	}

	/**
	 * Replaces a domain salt.
	 * 
	 * @param domainName domain name
	 * @param salt salt value; null is sent as an empty value
	 * @param allowEmpty whether empty salts are accepted
	 * @return updated domain
	 */
	public Domain updateSalt(String domainName, String salt, boolean allowEmpty) {
		return request(HttpMethod.PUT, new String[] {"api", "domains", required(domainName), "salt"},
				Map.of("salt", salt == null ? "" : salt, "allowEmpty", allowEmpty), null, Set.of(200));
	}

	/**
	 * Searches domains; HTTP 206 is exposed as a partial result.
	 * 
	 * @param query search query
	 * @return search result
	 */
	public SearchResult<Domain> search(String query) {
		var response = http.exchange(HttpMethod.GET,
				http.uri(new String[] {"api", "domains"}, Map.of("query", required(query))), 
				null, new ParameterizedTypeReference<List<Domain>>() {}, Set.of(200, 206), true);

		return new SearchResult<>(response.getBody(), response.getStatus() == 206);
	}

	/**
	 * Executes an authenticated request and deserializes its response body as a domain.
	 *
	 * @param method the HTTP method to use
	 * @param path the individual URI path segments
	 * @param query the query parameters to include
	 * @param body the request body, or {@code null} when no body is required
	 * @param statuses the expected HTTP status codes
	 * @return the domain returned by TrustDeck, or {@code null} when the response has no body
	 */
	private Domain request(HttpMethod method, String[] path, Map<String, ?> query, Object body, Set<Integer> statuses) {
		return request(method, path, query, body, Domain.class, statuses);
	}

	/**
	 * Executes an authenticated request and deserializes its response body as the requested type.
	 *
	 * @param <T> the expected response body type
	 * @param method the HTTP method to use
	 * @param path the individual URI path segments
	 * @param query the query parameters to include
	 * @param body the request body, or {@code null} when no body is required
	 * @param type the class representing the expected response body type
	 * @param statuses the expected HTTP status codes
	 * @return the deserialized response body, or {@code null} when the response has no body
	 */
	private <T> T request(HttpMethod method, String[] path, Map<String, ?> query, Object body, Class<T> type, Set<Integer> statuses) {
		return http.exchange(method, http.uri(path, query), body, type, statuses, true).getBody();
	}

	/**
	 * Executes an authenticated request and deserializes its response body as a list of domain trees.
	 *
	 * @param method the HTTP method to use
	 * @param path the individual URI path segments
	 * @param query the query parameters to include
	 * @param body the request body, or {@code null} when no body is required
	 * @param statuses the expected HTTP status codes
	 * @return the domain trees returned by TrustDeck, or {@code null} when the response has no body
	 */
	private List<DomainTree> list(HttpMethod method, String[] path, Map<String, ?> query, Object body, Set<Integer> statuses) {
		return http.exchange(method, http.uri(path, query), body, 
				new ParameterizedTypeReference<List<DomainTree>>() {}, statuses, true).getBody();
	}

	/**
	 * Validates that a required domain-related value is neither {@code null} nor blank.
	 *
	 * @param value the value to validate
	 * @return the validated value
	 * @throws IllegalArgumentException when the value is {@code null} or blank
	 */
	private static String required(String value) {
		return TrustDeckHttpClient.require(value, "value");
	}
}
