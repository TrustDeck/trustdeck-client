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

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.Project;
import org.trustdeck.client.model.ProjectDomain;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Synchronous operations for TrustDeck projects.
 *
 * @author Armin Müller
 */
public class Projects {

	/** Shared HTTP transport object. */
	private final TrustDeckHttpClient http;

	/**
	 * Creates the service.
	 *
	 * @param http shared HTTP transport object
	 */
	public Projects(TrustDeckHttpClient http) {
		this.http = http;
	}

	/**
	 * Creates a project.
	 *
	 * @param project the project definition
	 * @return created project
	 */
	public Project create(Project project) {
		return request(HttpMethod.POST, new String[] {"api", "projects"}, Map.of(), project, Set.of(200, 201));
	}

	/**
	 * Lists all projects.
	 *
	 * @return projects
	 */
	public List<Project> getAll() {
		return http.exchange(HttpMethod.GET, http.uri(new String[] { "api", "projects" }, Map.of()), null,
				new ParameterizedTypeReference<List<Project>>() { }, Set.of(200), true).getBody();
	}

	/**
	 * Gets a project.
	 *
	 * @param projectAbbreviation project abbreviation
	 * @return project
	 */
	public Project get(String projectAbbreviation) {
		return request(HttpMethod.GET, path(projectAbbreviation), Map.of(), null, Set.of(200));
	}

	/**
	 * Lists all domains in a project.
	 *
	 * @param projectAbbreviation project abbreviation
	 * @return project domains
	 */
	public List<ProjectDomain> getDomains(String projectAbbreviation) {
		return http.exchange(HttpMethod.GET, http.uri(path(projectAbbreviation, "domains"), Map.of()), null,
				new ParameterizedTypeReference<List<ProjectDomain>>() { }, Set.of(200), true).getBody();
	}

	/**
	 * Gets project statistics.
	 *
	 * @param projectAbbreviation project abbreviation
	 * @return statistics JSON
	 */
	public JsonNode getStatistics(String projectAbbreviation) {
		return http.exchange(HttpMethod.GET, http.uri(path(projectAbbreviation, "statistics"), Map.of()), null,
				JsonNode.class, Set.of(200), true).getBody();
	}

	/**
	 * Updates a project.
	 *
	 * @param projectAbbreviation project abbreviation
	 * @param updateProject project definition
	 * @return the updated project, when successful
	 */
	public Project update(String projectAbbreviation, Project updateProject) {
		return request(HttpMethod.PUT, path(projectAbbreviation), Map.of(), updateProject, Set.of(200));
	}

	/**
	 * Deletes a project, optionally at a supplied date.
	 *
	 * @param projectAbbreviation project abbreviation
	 * @param deleteDate scheduled deletion date, or {@code null}
	 * @return {@code true} when accepted
	 */
	public boolean delete(String projectAbbreviation, OffsetDateTime deleteDate) {
		LinkedHashMap<String, Object> reqParam = new LinkedHashMap<String, Object>();
		reqParam.put("deleteDate", deleteDate);

		// The API accepts a null delete date to request immediate deletion
		http.empty(HttpMethod.DELETE, http.uri(path(projectAbbreviation), reqParam), null, Set.of(204), true);
		
		return true;
	}

	/**
	 * Deletes a project immediately.
	 *
	 * @param projectAbbreviation project abbreviation
	 * @return {@code true} when accepted
	 */
	public boolean delete(String projectAbbreviation) {
		return delete(projectAbbreviation, null);
	}

	/**
	 * Executes an authenticated request and deserializes the response body as a project.
	 *
	 * @param method HTTP method to use
	 * @param path path segments identifying the endpoint
	 * @param query query parameters to include
	 * @param body request body to send, or {@code null} if the request has no body
	 * @param expected acceptable HTTP response status codes
	 * @return the project returned by TrustDeck, or {@code null} if the response has no body
	 */
	private Project request(HttpMethod method, String[] path, Map<String, ?> query, Object body, Set<Integer> expected) {
		return http.exchange(method, http.uri(path, query), body, Project.class, expected, true).getBody();
	}

	/**
	 * Builds a project endpoint path and validates its project segment.
	 *
	 * @param projectAbbreviation project abbreviation to include in the path
	 * @param suffix additional path segments to append
	 * @return the complete project endpoint path segments
	 */
	private static String[] path(String projectAbbreviation, String... suffix) {
		String[] path = new String[3 + suffix.length];
		path[0] = "api";
		path[1] = "projects";
		path[2] = TrustDeckHttpClient.require(projectAbbreviation, "projectAbbreviation");
		System.arraycopy(suffix, 0, path, 3, suffix.length);
		
		return path;
	}
}
