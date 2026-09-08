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
 * Operations for globally defined base entity types.
 *
 * @author Armin Müller
 */
public class BaseEntityTypes {

	/** Shared HTTP transport object. */
	private final TrustDeckHttpClient http;

	/**
	 * Creates the service.
	 *
	 * @param http shared HTTP transport
	 */
	public BaseEntityTypes(TrustDeckHttpClient http) {
		this.http = http;
	}

	/**
	 * Creates a base type.
	 *
	 * @param type type definition
	 * @return created type
	 */
	public EntityType create(EntityType type) {
		return request(HttpMethod.POST, new String[] { "api", "entities", "base-types" }, Map.of(), type, Set.of(200, 201));
	}

	/**
	 * Gets a base type.
	 *
	 * @param name type name
	 * @return type definition
	 */
	public EntityType get(String name) {
		return request(HttpMethod.GET,
				new String[] { "api", "entities", "base-types", TrustDeckHttpClient.require(name, "entityTypeName") },
				Map.of(), null, Set.of(200));
	}

	/**
	 * Searches base types.
	 *
	 * @param query search query
	 * @return search result
	 */
	public SearchResult<EntityType> search(String query) {
		Response<List<EntityType>> response = http.exchange(HttpMethod.GET,
				http.uri(new String[] { "api", "entities", "base-types" }, Map.of("query", TrustDeckHttpClient.require(query, "query"))),
				null,
				new ParameterizedTypeReference<List<EntityType>>() { },
				Set.of(200, 206),
				true);

		// HTTP 206 indicates that the backend could only return a partial result
		return new SearchResult<>(response.getBody(), response.getStatus() == 206);
	}

	/**
	 * Executes a typed request and returns its response body.
	 * 
	 * @param method the http method that should be executed on the URI
	 * @param path the URI
	 * @param params the request parameters
	 * @param body the request body if the method is POST or UPDATE
	 * @param statuses a list of expected status codes for this request
	 * @return the entity type returned by TrustDeck
	 */
	private EntityType request(HttpMethod method, String[] path, Map<String, ?> params, Object body, Set<Integer> statuses) {
		return http.exchange(method, http.uri(path, params), body, EntityType.class, statuses, true).getBody();
	}
}
