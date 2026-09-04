package org.trustdeck.client.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.Project;
import org.trustdeck.client.model.ProjectDomain;

import com.fasterxml.jackson.databind.JsonNode;

/** Synchronous operations for TrustDeck projects. */
public class Projects {
	private final TrustDeckHttpClient http;
	/** Creates the service.
	 * @param http shared HTTP transport
	 */
	public Projects(TrustDeckHttpClient http) { this.http = http; }
	/** Creates a project.
	 * @param value project definition
	 * @return created project
	 */
	public Project create(Project value) { return call(HttpMethod.POST, new String[] { "api", "projects" }, Map.of(), value, Set.of(200, 201)); }
	/** Lists projects.
	 * @return projects
	 */
	public List<Project> getAll() { return http.exchange(HttpMethod.GET, http.uri(new String[] { "api", "projects" }, Map.of()), null, new ParameterizedTypeReference<List<Project>>() {}, Set.of(200), true).getBody(); }
	/** Gets a project.
	 * @param project project abbreviation
	 * @return project
	 */
	public Project get(String project) { return call(HttpMethod.GET, path(project), Map.of(), null, Set.of(200)); }
	/** Lists project domains.
	 * @param project project abbreviation
	 * @return project domains
	 */
	public List<ProjectDomain> getDomains(String project) { return http.exchange(HttpMethod.GET, http.uri(path(project, "domains"), Map.of()), null, new ParameterizedTypeReference<List<ProjectDomain>>() {}, Set.of(200), true).getBody(); }
	/** Gets project statistics.
	 * @param project project abbreviation
	 * @return statistics JSON
	 */
	public JsonNode getStatistics(String project) { return http.exchange(HttpMethod.GET, http.uri(path(project, "statistics"), Map.of()), null, JsonNode.class, Set.of(200), true).getBody(); }
	/** Updates a project.
	 * @param project project abbreviation
	 * @param value project definition
	 * @return updated project, when returned
	 */
	public Optional<Project> update(String project, Project value) { return Optional.ofNullable(call(HttpMethod.PUT, path(project), Map.of(), value, Set.of(200))); }
	/** Deletes a project, optionally at a supplied date.
	 * @param project project abbreviation
	 * @param deleteDate scheduled deletion date, or {@code null}
	 * @return {@code true} when accepted
	 */
	public boolean delete(String project, OffsetDateTime deleteDate) { java.util.Map<String, Object> query = new java.util.LinkedHashMap<>(); query.put("deleteDate", deleteDate); http.empty(HttpMethod.DELETE, http.uri(path(project), query), null, Set.of(204), true); return true; }
	/** Deletes a project immediately.
	 * @param project project abbreviation
	 * @return {@code true} when accepted
	 */
	public boolean deleteNow(String project) { return delete(project, null); }
	private Project call(HttpMethod method, String[] path, Map<String, ?> query, Object body, Set<Integer> expected) { return http.exchange(method, http.uri(path, query), body, Project.class, expected, true).getBody(); }
	private static String[] path(String project, String... suffix) { String[] path = new String[3 + suffix.length]; path[0] = "api"; path[1] = "projects"; path[2] = TrustDeckHttpClient.require(project, "projectAbbreviation"); System.arraycopy(suffix, 0, path, 3, suffix.length); return path; }
}
