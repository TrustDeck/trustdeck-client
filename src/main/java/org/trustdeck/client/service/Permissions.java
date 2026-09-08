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
import org.trustdeck.client.model.BatchResult;
import org.trustdeck.client.model.EffectivePermission;
import org.trustdeck.client.model.Permission;
import org.trustdeck.client.model.SearchResult;
import org.trustdeck.client.model.User;

/**
 * Permission and user-management operations.
 *
 * @author Armin Müller
 */
public class Permissions {

	/** Shared HTTP transport object. */
	private final TrustDeckHttpClient http;

	/**
	 * Creates the permission service.
	 *
	 * @param http shared HTTP transport
	 */
	public Permissions(TrustDeckHttpClient http) {
		this.http = http;
	}

	/**
	 * Searches users and exposes HTTP 206 as partial.
	 * 
	 * @param query search query
	 * @return user search result
	 */
	public SearchResult<User> searchUsers(String query) {
		Response<List<User>> response = http.exchange(HttpMethod.GET,
				http.uri(path("users"), Map.of("query", TrustDeckHttpClient.require(query, "query"))), 
				null, new ParameterizedTypeReference<List<User>>() { }, Set.of(200, 206), true);

		// HTTP 206 indicates that the backend could only return a partial result.
		return new SearchResult<>(response.getBody(), response.getStatus() == 206);
	}

	/**
	 * Creates domain permissions.
	 * 
	 * @param domainName domain name
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return batch result
	 */
	public BatchResult<Permission> createDomain(String domainName, String user, List<Permission> permissions) {
		return create(path("domains", domainName), user, permissions);
	}

	/**
	 * Creates project permissions.
	 * 
	 * @param projectAbbreviation project abbreviation
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return batch result
	 */
	public BatchResult<Permission> createProject(String projectAbbreviation, String user, List<Permission> permissions) {
		return create(path("projects", projectAbbreviation), user, permissions);
	}

	/**
	 * Creates global permissions.
	 * 
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return batch result
	 */
	public BatchResult<Permission> createGlobal(String user, List<Permission> permissions) {
		return create(path("global"), user, permissions);
	}

	/**
	 * Creates entity-type permissions.
	 * 
	 * @param projectAbbreviation project abbreviation
	 * @param typeName entity type name
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return batch result
	 */
	public BatchResult<Permission> createEntityType(String projectAbbreviation, String typeName, String user, List<Permission> permissions) {
		return create(path("projects", projectAbbreviation, "entity-types", typeName), user, permissions);
	}

	/**
	 * Gets domain permissions.
	 * 
	 * @param domainName domain name
	 * @param user subject identifier
	 * @return permissions
	 */
	public List<Permission> getDomain(String domainName, String user) {
		return get(path("domains", domainName), user);
	}

	/**
	 * Gets project permissions.
	 * 
	 * @param projectAbbreviation project abbreviation
	 * @param user subject identifier
	 * @return permissions
	 */
	public List<Permission> getProject(String projectAbbreviation, String user) {
		return get(path("projects", projectAbbreviation), user);
	}

	/**
	 * Gets global permissions.
	 * 
	 * @param user subject identifier
	 * @return permissions
	 */
	public List<Permission> getGlobal(String user) {
		return get(path("global"), user);
	}

	/**
	 * Gets entity-type permissions.
	 * 
	 * @param projectAbbreviation project abbreviation
	 * @param typeName entity type name
	 * @param user subject identifier
	 * @return permissions
	 */
	public List<Permission> getEntityType(String projectAbbreviation, String typeName, String user) {
		return get(path("projects", projectAbbreviation, "entity-types", typeName), user);
	}

	/**
	 * Gets permissions available to the current user.
	 * 
	 * @return available permissions
	 */
	public List<EffectivePermission> getAvailable() {
		return http.exchange(HttpMethod.GET, http.uri(path(), Map.of()), null,
				new ParameterizedTypeReference<List<EffectivePermission>>() {}, Set.of(200), true).getBody();
	}

	/**
	 * Updates domain permissions.
	 * 
	 * @param domainName domain name
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return {@code true} after HTTP 200
	 */
	public boolean updateDomain(String domainName, String user, List<Permission> permissions) {
		return update(path("domains", domainName), user, permissions);
	}

	/**
	 * Updates project permissions.
	 * 
	 * @param projectAbbreviation project abbreviation
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return {@code true} after HTTP 200
	 */
	public boolean updateProject(String projectAbbreviation, String user, List<Permission> permissions) {
		return update(path("projects", projectAbbreviation), user, permissions);
	}

	/**
	 * Updates global permissions.
	 * 
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return {@code true} after HTTP 200
	 */
	public boolean updateGlobal(String user, List<Permission> permissions) {
		return update(path("global"), user, permissions);
	}

	/**
	 * Updates entity-type permissions.
	 * 
	 * @param projectAbbreviation project abbreviation
	 * @param typeName entity type name
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return {@code true} after HTTP 200
	 */
	public boolean updateEntityType(String projectAbbreviation, String typeName, String user, List<Permission> permissions) {
		return update(path("projects", projectAbbreviation, "entity-types", typeName), user, permissions);
	}

	/**
	 * Deletes domainName permissions and preserves batch status.
	 * 
	 * @param domainName domain name
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteDomain(String domainName, String user, List<Permission> permissions) {
		return delete(path("domains", domainName), user, permissions);
	}

	/**
	 * Deletes project permissions.
	 * 
	 * @param projectAbbreviation project abbreviation
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteProject(String projectAbbreviation, String user, List<Permission> permissions) {
		return delete(path("projects", projectAbbreviation), user, permissions);
	}

	/**
	 * Deletes global permissions.
	 * 
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteGlobal(String user, List<Permission> permissions) {
		return delete(path("global"), user, permissions);
	}

	/**
	 * Deletes entity-type permissions.
	 * 
	 * @param projectAbbreviation project abbreviation
	 * @param typeName entity type name
	 * @param user subject identifier
	 * @param permissions the list of permissions/actions to be granted
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteEntityType(String projectAbbreviation, String typeName, String user, List<Permission> permissions) {
		return delete(path("projects", projectAbbreviation, "entity-types", typeName), user, permissions);
	}

	/**
	 * Creates permissions for a user within the specified scope.
	 *
	 * @param path path segments identifying the permission scope
	 * @param user ID of the user receiving the permissions
	 * @param permissions permissions to create
	 * @return the created permissions together with the HTTP status and an indicator
	 *         of whether the operation was only partially successful
	 */
	private BatchResult<Permission> create(String[] path, String user, List<Permission> permissions) {
		Response<List<Permission>> response = http.exchange(HttpMethod.POST, http.uri(path, user(user)), permissions,
				new ParameterizedTypeReference<List<Permission>>() {}, Set.of(200, 201, 206), true);
		
		return new BatchResult<>(response.getBody(), response.getStatus(), response.getStatus() == 206);
	}

	/**
	 * Retrieves the permissions assigned to a user within the specified scope.
	 *
	 * @param path path segments identifying the permission scope
	 * @param user ID of the user whose permissions should be retrieved
	 * @return the permissions assigned to the user
	 */
	private List<Permission> get(String[] path, String user) {
		return http.exchange(HttpMethod.GET, http.uri(path, user(user)), null,
				new ParameterizedTypeReference<List<Permission>>() {}, Set.of(200), true).getBody();
	}

	/**
	 * Updates the permissions assigned to a user within the specified scope.
	 *
	 * @param path path segments identifying the permission scope
	 * @param user ID of the user whose permissions should be updated
	 * @param permissions replacement permissions to assign
	 * @return {@code true} if the request completes successfully
	 */
	private boolean update(String[] path, String user, List<Permission> permissions) {
		http.empty(HttpMethod.PUT, http.uri(path, user(user)), permissions, Set.of(200), true);
		
		return true;
	}

	/**
	 * Deletes permissions from a user within the specified scope.
	 *
	 * @param path path segments identifying the permission scope
	 * @param user ID of the user whose permissions should be deleted
	 * @param permissions permissions to delete
	 * @return the deletion results together with the HTTP status and an indicator
	 *         of whether the operation was only partially successful
	 */
	private BatchResult<Boolean> delete(String[] path, String user, List<Permission> permissions) {
		Response<List<Boolean>> response = http.exchange(HttpMethod.DELETE, http.uri(path, user(user)), permissions,
				new ParameterizedTypeReference<List<Boolean>>() {}, Set.of(204, 206), true);

		// A 204 response has no body, so expose it as an empty batch result
		List<Boolean> results = response.getBody() == null ? List.<Boolean>of() : response.getBody();
		return new BatchResult<>(results, response.getStatus(), response.getStatus() == 206);
	}

	/**
	 * Creates the query parameters identifying a user.
	 *
	 * @param userId user ID to validate and include
	 * @return query parameters containing the validated user ID
	 * @throws IllegalArgumentException if the user ID is {@code null} or blank
	 */
	private static Map<String, Object> user(String userId) {
		return Map.of("userId", TrustDeckHttpClient.require(userId, "userId"));
	}

	/**
	 * Builds a permission endpoint path from the supplied suffix segments.
	 *
	 * @param suffix path segments to append to the permission API base path
	 * @return the complete permission endpoint path segments
	 * @throws IllegalArgumentException if a suffix segment is {@code null} or blank
	 */
	private static String[] path(String... suffix) {
		String[] path = new String[2 + suffix.length];
		path[0] = "api";
		path[1] = "permissions";
		
		for (int i = 0; i < suffix.length; i++) {
			path[i + 2] = TrustDeckHttpClient.require(suffix[i], "path value");
		}
		
		return path;
	}
}
