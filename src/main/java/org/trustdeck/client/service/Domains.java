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
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.DomainTree;
import org.trustdeck.client.model.SearchResult;

/**
 * Global domain search and hierarchy operations.
 *
 * @author Armin Müller
 */
public class Domains {

	/** Shared HTTP client used for requests. */
	private final TrustDeckHttpClient http;

	/**
	 * Creates the global domain service.
	 * 
	 * @param http shared HTTP transport
	 */
	public Domains(TrustDeckHttpClient http) {
		this.http = http;
	}

	/**
	 * Searches all domains; HTTP 206 is exposed as a partial result.
	 * 
	 * @param query search query
	 * @return domain search result
	 */
	public SearchResult<Domain> search(String query) {
		Response<List<Domain>> response = http.exchange(HttpMethod.GET,
				http.uri(new String[] {"api", "domains"}, Map.of("query", TrustDeckHttpClient.require(query, "query"))),
				null, new ParameterizedTypeReference<List<Domain>>() { }, Set.of(200, 206), true);
		
		return new SearchResult<>(response.getBody(), response.getStatus() == 206);
	}

	/**
	 * Retrieves the global domain hierarchy.
	 * @return domain hierarchy
	 */
	public List<DomainTree> getHierarchy() {
		return http.exchange(HttpMethod.GET, http.uri(new String[] {"api", "domains", "hierarchy"}, Map.of()), null,
				new ParameterizedTypeReference<List<DomainTree>>() { }, Set.of(200), true).getBody();
	}
}
