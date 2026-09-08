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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.Response;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.BatchResult;
import org.trustdeck.client.model.IdentifierItem;
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.model.PseudonymUpdate;
import org.trustdeck.client.model.SearchResult;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Synchronous operations for a scoped TrustDeck pseudonym domain.
 *
 * @author Armin Müller
 */
public class Pseudonyms {
	
	/** Shared HTTP transport object. */
	private final TrustDeckHttpClient http;
	
	/** Domain name. */
	private final String domainName;
	
	/**
	 * Creates a domain-scoped pseudonym service.
	 * 
	 * @param http shared HTTP transport object
	 * @param domainName domain name
	 */
	public Pseudonyms(TrustDeckHttpClient http, String domainName) {
		this.http = http;
		this.domainName = TrustDeckHttpClient.require(domainName, "domainName");
	}

	/**
	 * Creates pseudonyms in a batch; HTTP 206 is partial completion.
	 * 
	 * @param pseudonyms a list of pseudonym payloads
	 * @param omitPrefix whether prefixes are omitted
	 * @return batch result
	 */
	public BatchResult<Pseudonym> createBatch(List<Pseudonym> pseudonyms, boolean omitPrefix) {
		Response<List<Pseudonym>> response = http.exchange(HttpMethod.POST, http.uri(path("batch"), 
				Map.of("omitPrefix", omitPrefix)), pseudonyms,
				new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(201, 206), true);

		return new BatchResult<>(response.getBody(), response.getStatus(), response.getStatus() == 206);
	}

	/**
	 * Creates one pseudonym.
	 * 
	 * @param pseudonym the pseudonym payload
	 * @param omitPrefix whether the prefix is omitted
	 * @return created pseudonym
	 */
	public Pseudonym create(Pseudonym pseudonym, boolean omitPrefix) {
		return request(HttpMethod.POST, path(), Map.of("omitPrefix", omitPrefix), pseudonym, Set.of(200, 201));
	}

	/**
	 * Creates one pseudonym using the default prefix behavior.
	 * 
	 * @param pseudonym the pseudonym payload
	 * @return created pseudonym
	 */
	public Pseudonym create(Pseudonym pseudonym) {
		return create(pseudonym, false);
	}

	/**
	 * Creates one pseudonym using an identifier item.
	 * 
	 * @param identifierItem identifier item
	 * @param omitPrefix whether the prefix is omitted
	 * @return created pseudonym
	 */
	public Pseudonym create(IdentifierItem identifierItem, boolean omitPrefix) {
		return create(Pseudonym.builder().identifierItem(identifierItem).build(), omitPrefix);
	}

	/**
	 * Creates one pseudonym using an identifier item and using the default omitPrefix behavior.
	 * 
	 * @param identifierItem identifier item
	 * @return created pseudonym
	 */
	public Pseudonym create(IdentifierItem identifierItem) {
		return create(Pseudonym.builder().identifierItem(identifierItem).build(), false);
	}

	/**
	 * Creates one pseudonym using identifier values.
	 * 
	 * @param identifier identifying value
	 * @param idType the identifier's type
	 * @param omitPrefix whether the prefix is omitted
	 * @return created pseudonym
	 */
	public Pseudonym create(String identifier, String idType, boolean omitPrefix) {
		return create(IdentifierItem.builder().identifier(identifier).idType(idType).build(), omitPrefix);
	}

	/**
	 * Creates one pseudonym using identifier values and using the default omitPrefix behavior.
	 * 
	 * @param identifier identifying value
	 * @param idType the identifier's type
	 * @return created pseudonym
	 */
	public Pseudonym create(String identifier, String idType) {
		return create(IdentifierItem.builder().identifier(identifier).idType(idType).build(), false);
	}

	/**
	 * Retrieves linked pseudonyms.
	 * 
	 * @param sourceDomain the domain of the source pseudonym
	 * @param targetDomain the domain of the target pseudonym
	 * @param identifier optional source identifier
	 * @param idType optional source identifier type
	 * @param psn optional source pseudonym
	 * @return linked pseudonym pairs
	 */
	public List<Pair<Pseudonym, Pseudonym>> getLinkedPseudonyms(String sourceDomain, String targetDomain, String identifier, String idType, String psn) {
		Response<List<Pair<Pseudonym, Pseudonym>>> response = http.exchange(HttpMethod.GET, 
				http.uri(new String[] {"api", "domains", "linked-pseudonyms"},
				query(sourceDomain, targetDomain, identifier, idType, psn)), null,
				new ParameterizedTypeReference<List<Pair<Pseudonym, Pseudonym>>>() {}, Set.of(200), true);

		return response.getBody();
	}

	/**
	 * Gets a pseudonym by its identifier item.
	 * 
	 * @param identifierItem identifier item
	 * @return pseudonym
	 */
	public Pseudonym get(IdentifierItem identifierItem) {
		return request(HttpMethod.GET, path(), 
				Map.of("id", TrustDeckHttpClient.require(identifierItem.getIdentifier(), "identifier"),
				"idType", TrustDeckHttpClient.require(identifierItem.getIdType(), "idType")), null, Set.of(200));
	}

	/**
	 * Gets a pseudonym by its pseudonym value.
	 * 
	 * @param psn pseudonym value
	 * @return pseudonym
	 */
	public Pseudonym get(String psn) {
		return request(HttpMethod.GET, path(), Map.of("psn", TrustDeckHttpClient.require(psn, "psn")), null, Set.of(200));
	}

	/**
	 * Gets all pseudonyms in the domain.
	 * 
	 * @return pseudonyms
	 */
	public List<Pseudonym> getBatch() {
		return http.exchange(HttpMethod.GET, http.uri(path("batch"), Map.of()), null,
				new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(200), true).getBody();
	}

	/**
	 * Updates pseudonyms in a batch.
	 * 
	 * @param updates update payloads
	 * @return updated pseudonyms
	 */
	public List<Pseudonym> updateBatch(List<PseudonymUpdate> updates) {
		return http.exchange(HttpMethod.PUT, http.uri(path("batch"), Map.of()), updates,
				new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(200), true).getBody();
	}

	/**
	 * Updates one pseudonym.
	 * 
	 * @param update update payload
	 * @return updated pseudonym
	 */
	public Pseudonym update(PseudonymUpdate update) {
		return request(HttpMethod.PUT, path(), Map.of(), update, Set.of(200));
	}

	/**
	 * Updates one pseudonym and optionally regenerates its value.
	 * 
	 * @param update update payload
	 * @param regenerate whether to regenerate the psn-value if necessary
	 * @return updated pseudonym
	 */
	public Pseudonym updateComplete(PseudonymUpdate update, Boolean regenerate) {
		return request(HttpMethod.PUT, path("complete"), Map.of("regeneratePseudonym", regenerate), update, Set.of(200));
	}

	/**
	 * Deletes all pseudonyms in a batch; HTTP 206 is partial completion.
	 * 
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteBatch() {
		Response<List<Boolean>> response = http.exchange(HttpMethod.DELETE, http.uri(path("batch"), Map.of()), null,
				new ParameterizedTypeReference<List<Boolean>>() {}, Set.of(204, 206), true);

		return new BatchResult<>(response.getBody() == null ? List.of() : response.getBody(), response.getStatus(), response.getStatus() == 206);
	}

	/**
	 * Deletes a pseudonym by identifier.
	 * 
	 * @param identifierItem identifier item
	 * @return {@code true} after deletion
	 */
	public boolean delete(IdentifierItem identifierItem) {
		http.empty(HttpMethod.DELETE, 
				http.uri(path(), Map.of("id", TrustDeckHttpClient.require(identifierItem.getIdentifier(), "identifier"),
				"idType", TrustDeckHttpClient.require(identifierItem.getIdType(), "idType"))), null, Set.of(204), true);

		return true;
	}

	/**
	 * Deletes a pseudonym by its psn-value.
	 * 
	 * @param psn pseudonym value
	 * @return {@code true} after deletion
	 */
	public boolean delete(String psn) {
		http.empty(HttpMethod.DELETE, http.uri(path(), Map.of("psn", TrustDeckHttpClient.require(psn, "psn"))), null, Set.of(204), true);

		return true;
	}

	/**
	 * Validates a pseudonym value, i.e. validates the check digit.
	 * 
	 * @param psn pseudonym value
	 * @return validation result
	 */
	public boolean validate(String psn) {
		return http.exchange(HttpMethod.GET, http.uri(path("validation"), 
				Map.of("psn", TrustDeckHttpClient.require(psn, "psn"))),
				null, Boolean.class, Set.of(200), true).getBody();
	}

	/**
	 * Searches pseudonyms.
	 * 
	 * @param query search query
	 * @return search result and an indicator if the search result is a partial result
	 */
	public SearchResult<Pseudonym> search(String query) {
		Response<List<Pseudonym>> response = http.exchange(HttpMethod.GET, http.uri(path(), 
				Map.of("query", TrustDeckHttpClient.require(query, "query"))), null,
				new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(200, 206), true);

		return new SearchResult<>(response.getBody(), response.getStatus() == 206);
	}
	
	/**
	 * Builds the domain-scoped pseudonym endpoint path.
	 *
	 * @param suffix additional path segments to append
	 * @return the complete domain endpoint path segments
	 */
	private String[] path(String... suffix) {
		String[] path = new String[4 + suffix.length];
		path[0] = "api";
		path[1] = "domains";
		path[2] = domainName;
		path[3] = "pseudonyms";
		System.arraycopy(suffix, 0, path, 4, suffix.length);

		return path;
	}

	/**
	 * Executes an authenticated request and deserializes the response body as a pseudonym.
	 *
	 * @param method HTTP method to use
	 * @param path path segments identifying the endpoint
	 * @param query query parameters to include
	 * @param body request body to send, or {@code null} if the request has no body
	 * @param expected acceptable HTTP response status codes
	 * @return the pseudonym returned by TrustDeck, or {@code null} if the response has no body
	 */
	private Pseudonym request(HttpMethod method, String[] path, Map<String, ?> query, Object body, Set<Integer> expected) {
		return http.exchange(method, http.uri(path, query), body, Pseudonym.class, expected, true).getBody();
	}

	/**
	 * Builds the query parameters used to retrieve linked pseudonyms.
	 *
	 * @param source source domain name
	 * @param target target domain name
	 * @param identifier optional source identifier
	 * @param idType optional source identifier type
	 * @param psn optional source pseudonym
	 * @return the linked-pseudonym query parameters
	 */
	private static Map<String, Object> query(String source, String target, String identifier, String idType, String psn) {
		LinkedHashMap<String, Object> query = new LinkedHashMap<>();
		query.put("sourceDomain", TrustDeckHttpClient.require(source, "sourceDomain"));
		query.put("targetDomain", TrustDeckHttpClient.require(target, "targetDomain"));
		query.put("sourceIdentifier", identifier);
		query.put("sourceIdType", idType);
		query.put("sourcePsn", psn);

		return query;
	}
	
	/**
	 * Contains two related values.
	 *
	 * @param <A> type of the first value
	 * @param <B> type of the second value
	 * @param source source pseudonym
	 * @param target target pseudonym
	 */
	@JsonFormat(shape = JsonFormat.Shape.ARRAY)
	@JsonPropertyOrder({"source", "target"})
	public record Pair<A, B>(A source, B target) {}
}
