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
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.Response;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.exception.RecordLinkageConflictException;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.Entity;
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.model.RecordLinkageCandidate;
import org.trustdeck.client.model.RecordLinkageCandidate.RecordLinkageResolutionStrategy;
import org.trustdeck.client.model.SearchResult;

/**
 * Operations for entities scoped to a projectAbbreviation and typeName.
 *
 * @author Armin Müller
 */
public class Entities {
	
	/** Shared HTTP transport object. */
	private final TrustDeckHttpClient http;
	
	/** Project abbreviation. */
	private final String projectAbbreviation;
	
	/** Entity typeName name. */
	private final String typeName;
	
	/**
	 * Creates a projectAbbreviation/typeName-scoped entity service.
	 * 
	 * @param http shared HTTP transport
	 * @param projectAbbreviation projectAbbreviation abbreviation
	 * @param entityTypeName entity typeName name
	 */
	public Entities(TrustDeckHttpClient http, String projectAbbreviation, String entityTypeName) {
		this.http = http;
		this.projectAbbreviation = TrustDeckHttpClient.require(projectAbbreviation, "projectAbbreviation");
		this.typeName = TrustDeckHttpClient.require(entityTypeName, "entityTypeName");
	}

	/**
	 * Creates an entity without a record-linkage resolution.
	 * 
	 * @param entity entity payload
	 * @return created entity
	 */
	public Entity create(Entity entity) {
		return create(entity, null);
	}

	/**
	 * Creates an entity with optional record-linkage resolution.
	 * 
	 * @param entity entity payload
	 * @param resolutionStrategy optional record-linkage decision
	 * @return created entity
	 * @throws RecordLinkageConflictException when resolution is required
	 */
	public Entity create(Entity entity, RecordLinkageResolutionStrategy resolutionStrategy) {
		Map<String, Object> query = new LinkedHashMap<>();
		query.put("recordLinkageResolution", resolutionStrategy);

		// Try the entity creation and catch record linkage errors
		try {
			return http.exchange(HttpMethod.POST, http.uri(path(), query), entity, Entity.class, Set.of(200, 201), true).getBody();
		} catch (TrustDeckResponseException exception) {
			// Check if the  encountered status code is record-linkage related or any other error
			if (exception.getStatusCode() != 409) {
				throw exception;
			}

			// A conflict body contains the candidates needed for the caller's decision
			try {
				List<RecordLinkageCandidate> candidates = http.mapper().readValue(exception.getRawBody(), 
						http.mapper().getTypeFactory().constructCollectionType(List.class, RecordLinkageCandidate.class));
				
				throw new RecordLinkageConflictException(exception.getResponseStatusCode(), exception.getStatusInfo(),
						exception.getRawBody(), exception.getContentType(), candidates);
			} catch (RecordLinkageConflictException conflict) {
				throw conflict;
			} catch (Exception parseFailure) {
				throw exception;
			}
		}
	}

	/**
	 * Gets an entity by its TrustDeckID.
	 * 
	 * @param trustDeckId the entity identifier
	 * @return entity
	 */
	public Entity get(UUID trustDeckId) {
		return request(HttpMethod.GET, path(trustDeckId), Map.of(), null, Set.of(200));
	}

	/**
	 * Updates an entity.
	 * 
	 * @param trustDeckId the entity identifier
	 * @param updateEntity replacement payload
	 * @return updated entity
	 */
	public Entity update(UUID trustDeckId, Entity updateEntity) {
		return request(HttpMethod.PUT, path(trustDeckId), Map.of(), updateEntity, Set.of(200));
	}

	/**
	 * Deletes an entity after HTTP 204.
	 * 
	 * @param trustDeckId the entity identifier
	 * @return {@code true} after successful deletion
	 */
	public boolean delete(UUID trustDeckId) {
		http.empty(HttpMethod.DELETE, http.uri(path(trustDeckId), Map.of()), null, Set.of(204), true);

		return true;
	}

	/**
	 * Searches entities and exposes HTTP 206 as partial.
	 * 
	 * @param query search query
	 * @return search result
	 */
	public SearchResult<Entity> search(String query) {
		Response<List<Entity>> response = http.exchange(HttpMethod.GET,
				http.uri(path(), Map.of("query", TrustDeckHttpClient.require(query, "query"))), 
				null, new ParameterizedTypeReference<List<Entity>>() {}, Set.of(200, 206), true);

		return new SearchResult<>(response.getBody(), response.getStatus() == 206);
	}

	/**
	 * Lists pseudonyms associated with an entity.
	 * 
	 * @param trustDeckId the entity identifier
	 * @return search result
	 */
	public SearchResult<Pseudonym> getPseudonyms(UUID trustDeckId) {
		Response<List<Pseudonym>> response = http.exchange(HttpMethod.GET, http.uri(path(trustDeckId, "pseudonyms"), Map.of()), null,
				new ParameterizedTypeReference<List<Pseudonym>>() {}, Set.of(200, 206), true);

		return new SearchResult<>(response.getBody(), response.getStatus() == 206);
	}

	/**
	 * Finds record-linkage candidates for an entity.
	 * 
	 * @param entity entity payload
	 * @return candidate list, empty for HTTP 204
	 */
	public List<RecordLinkageCandidate> findRecordLinkageCandidates(Entity entity) {
		Response<List<RecordLinkageCandidate>> response = http.exchange(HttpMethod.POST, http.uri(path("record-linkage"), Map.of()), 
				entity, new ParameterizedTypeReference<List<RecordLinkageCandidate>>() {}, Set.of(200, 204), true);

		return response.getBody() == null ? List.of() : response.getBody();
	}

	/**
	 * Executes an authenticated request and deserializes the response body as an entity.
	 *
	 * @param method HTTP method to use
	 * @param path path segments identifying the endpoint
	 * @param query query parameters to include
	 * @param body entity to send, or {@code null} if the request has no body
	 * @param expected acceptable HTTP response status codes
	 * @return the entity returned by TrustDeck, or {@code null} if the response has no body
	 */
	private Entity request(HttpMethod method, String[] path, Map<String, ?> query, Entity body, Set<Integer> expected) {
		return http.exchange(method, http.uri(path, query), body, Entity.class, expected, true).getBody();
	}

	/**
	 * Builds the path segments for an entity endpoint by appending the supplied
	 * segments to the current project and entity-type path.
	 *
	 * @param suffix additional path segments to append
	 * @return the complete entity endpoint path segments
	 */
	private String[] path(Object... suffix) {
		String[] path = new String[5 + suffix.length];
		String[] prefix = {"api", "projects", projectAbbreviation, "entities", typeName};
		System.arraycopy(prefix, 0, path, 0, prefix.length);

		for (int i = 0; i < suffix.length; i++) {
			path[i + prefix.length] = String.valueOf(suffix[i]);
		}

		return path;
	}
}
