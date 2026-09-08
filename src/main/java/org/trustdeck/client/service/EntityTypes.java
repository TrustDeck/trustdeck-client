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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.Response;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.EntityType;
import org.trustdeck.client.model.SearchResult;

/**
 * Operations for entity types scoped to one projectAbbreviation.
 *
 * @author Armin Müller
 */
public class EntityTypes {

	/** Shared HTTP transport object. */
	private final TrustDeckHttpClient http;

	/** Project scope for this service. */
	private final String projectAbbreviation;

	/**
	 * Creates the service.
	 *
	 * @param http shared HTTP transport object
	 * @param projectAbbreviation the project's abbreviation
	 */
	public EntityTypes(TrustDeckHttpClient http, String projectAbbreviation) {
		this.http = http;
		this.projectAbbreviation = TrustDeckHttpClient.require(projectAbbreviation, "projectAbbreviation");
	}

	/**
	 * Creates an entity type.
	 *
	 * @param type type definition
	 * @return created type
	 */
	public EntityType create(EntityType type) {
		return request(HttpMethod.POST, configPath(), Map.of(), type, Set.of(200, 201));
	}

	/**
	 * Gets an entity type.
	 *
	 * @param typeName type name
	 * @return type definition
	 */
	public EntityType get(String typeName) {
		return request(HttpMethod.GET, configPath(TrustDeckHttpClient.require(typeName, "entityTypeName")), Map.of(), null, Set.of(200));
	}

	/**
	 * Updates an entity type.
	 *
	 * @param typeName type name
	 * @param updateEntity type definition
	 * @return updated type
	 */
	public EntityType update(String typeName, EntityType updateEntity) {
		return request(HttpMethod.PUT, configPath(TrustDeckHttpClient.require(typeName, "entityTypeName")), Map.of(), updateEntity, Set.of(200));
	}

	/**
	 * Deletes an entity type.
	 *
	 * @param typeName type name
	 * @return {@code true} when accepted
	 */
	public boolean delete(String typeName) {
		http.empty(HttpMethod.DELETE, http.uri(configPath(TrustDeckHttpClient.require(typeName, "entityTypeName")), 
				Map.of()), null, Set.of(204), true);
		
		return true;
	}

	/**
	 * Searches entity types.
	 *
	 * @param query search query
	 * @return search result
	 */
	public SearchResult<EntityType> search(String query) {
		Response<List<EntityType>> response = http.exchange(HttpMethod.GET,
				http.uri(new String[] {"api", "projects", projectAbbreviation, "entities"}, Map.of("query", TrustDeckHttpClient.require(query, "query"))), 
				null, new ParameterizedTypeReference<List<EntityType>>() { }, Set.of(200, 206), true);

		// HTTP 206 indicates that the backend could only return a partial result.
		return new SearchResult<>(response.getBody(), response.getStatus() == 206);
	}

	/**
	 * Builds the path segments for an entity-type configuration endpoint by
	 * appending the supplied segments to the current project's configuration path.
	 *
	 * @param suffix additional path segments to append
	 * @return the complete entity-type configuration endpoint path segments
	 */
	private String[] configPath(String... suffix) {
		String[] path = new String[5 + suffix.length];
		String[] prefix = { "api", "projects", projectAbbreviation, "entities", "config"};
		System.arraycopy(prefix, 0, path, 0, prefix.length);
		System.arraycopy(suffix, 0, path, prefix.length, suffix.length);
		
		return path;
	}

	/**
	 * Executes an authenticated request and deserializes the response body as an
	 * entity type.
	 *
	 * @param method HTTP method to use
	 * @param path path segments identifying the endpoint
	 * @param params query parameters to include
	 * @param body request body to send, or {@code null} if the request has no body
	 * @param statuses acceptable HTTP response status codes
	 * @return the entity type returned by TrustDeck, or {@code null} if the response has no body
	 */
	private EntityType request(HttpMethod method, String[] path, Map<String, ?> params, Object body, Set<Integer> statuses) {
		return http.exchange(method, http.uri(path, params), body, EntityType.class, statuses, true).getBody();
	}
}
