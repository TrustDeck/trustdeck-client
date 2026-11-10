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
import org.trustdeck.client.exception.PermissionsRejectedException;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.BatchResult;
import org.trustdeck.client.model.EffectivePermission;
import org.trustdeck.client.model.Permission;
import org.trustdeck.client.model.PermissionReplacement;
import org.trustdeck.client.model.SearchResult;
import org.trustdeck.client.model.User;

/**
 * Provides global permission operations and creates resource-scoped permission services.
 * 
 * @author Armin Müller
 */
public class Permissions {

	/** Shared HTTP client used for permission requests. */
	private final TrustDeckHttpClient http;

	/**
	 * Creates the global permission service.
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
				null, new ParameterizedTypeReference<List<User>>() {}, Set.of(200, 206), true);
		
		return new SearchResult<>(response.getBody(), response.getStatus() == 206);
	}

	/**
	 * Creates global permissions.
	 * 
	 * @param userId subject identifier
	 * @param permissions permissions to create
	 * @return created permissions
	 */
	public BatchResult<Permission> createGlobal(String userId, List<Permission> permissions) {
		return create(path("global"), userId, permissions);
	}

	/**
	 * Gets global permissions.
	 * 
	 * @param userId subject identifier
	 * @return permissions
	 */
	public List<Permission> getGlobal(String userId) {
		return get(path("global"), userId);
	}

	/**
	 * Updates global permissions.
	 * 
	 * @param userId subject identifier
	 * @param permissions the desired permissions; actions the requester may not grant stay unchanged
	 * @return the added and removed actions
	 * @throws PermissionsRejectedException when the update adds actions the requester may not grant; nothing is changed then
	 */
	public PermissionReplacement updateGlobal(String userId, List<Permission> permissions) {
		return update(path("global"), userId, permissions);
	}

	/**
	 * Deletes global permissions.
	 * 
	 * @param userId subject identifier
	 * @param permissions permissions to delete
	 * @return deletion results
	 */
	public BatchResult<Boolean> deleteGlobal(String userId, List<Permission> permissions) {
		return delete(path("global"), userId, permissions);
	}

	/**
	 * Gets the catalogue of all actions that can be granted.
	 * 
	 * @return available actions with their resource types
	 */
	public List<EffectivePermission> getAvailable() {
		return http.exchange(HttpMethod.GET, http.uri(path(), Map.of()), null,
				new ParameterizedTypeReference<List<EffectivePermission>>() {}, Set.of(200), true).getBody();
	}

	/**
	 * Gets the catalogue of the actions that can be granted for one resource type.
	 * 
	 * @param scope the resource type ({@code PROJECT}, {@code DOMAIN}, {@code ENTITY_TYPE}, or {@code GLOBAL})
	 * @return available actions of the resource type
	 */
	public List<EffectivePermission> getAvailable(String scope) {
		return http.exchange(HttpMethod.GET, http.uri(path(), Map.of("scope", TrustDeckHttpClient.require(scope, "scope"))), null,
				new ParameterizedTypeReference<List<EffectivePermission>>() {}, Set.of(200), true).getBody();
	}

	/**
	 * Gets the currently effective permissions of the authenticated user.
	 * 
	 * @return the user's effective permissions
	 */
	public List<EffectivePermission> getOwn() {
		return http.exchange(HttpMethod.GET, http.uri(path("me"), Map.of()), null,
				new ParameterizedTypeReference<List<EffectivePermission>>() {}, Set.of(200), true).getBody();
	}

	/**
	 * Creates permissions for a selected resource scope.
	 * This method has package-private visibility and is intended for use by the
	 * resource-scoped permission services.
	 *
	 * @param scope endpoint path identifying the resource scope
	 * @param userId ID of the user receiving the permissions
	 * @param permissions permissions to create
	 * @return result containing the created permissions and response status
	 */
	BatchResult<Permission> createScoped(String[] scope, String userId, List<Permission> permissions) {
		return create(scope, userId, permissions);
	}

	/**
	 * Retrieves permissions for a user within a selected resource scope.
	 * This method has package-private visibility and is intended for use by the
	 * resource-scoped permission services.
	 *
	 * @param scope endpoint path identifying the resource scope
	 * @param userId ID of the user whose permissions are retrieved
	 * @return permissions assigned to the user within the selected scope
	 */
	List<Permission> getScoped(String[] scope, String userId) {
		return get(scope, userId);
	}

	/**
	 * Updates permissions for a user within a selected resource scope.
	 * This method has package-private visibility and is intended for use by the
	 * resource-scoped permission services.
	 *
	 * @param scope endpoint path identifying the resource scope
	 * @param userId ID of the user whose permissions are updated
	 * @param permissions the desired permissions
	 * @return the added and removed actions
	 * @throws PermissionsRejectedException when the update adds actions the requester may not grant
	 */
	PermissionReplacement updateScoped(String[] scope, String userId, List<Permission> permissions) {
		return update(scope, userId, permissions);
	}

	/**
	 * Deletes permissions for a user within a selected resource scope.
	 * This method has package-private visibility and is intended for use by the
	 * resource-scoped permission services.
	 *
	 * @param scope endpoint path identifying the resource scope
	 * @param userId ID of the user whose permissions are deleted
	 * @param permissions permissions to delete
	 * @return result containing the deletion outcomes and response status
	 */
	BatchResult<Boolean> deleteScoped(String[] scope, String userId, List<Permission> permissions) {
		return delete(scope, userId, permissions);
	}

	/**
	 * Sends a permission-creation request for the specified resource scope.
	 *
	 * @param scope endpoint path identifying the resource scope
	 * @param userId ID of the user receiving the permissions
	 * @param permissions permissions to create
	 * @return result containing the created permissions and response status
	 */
	private BatchResult<Permission> create(String[] scope, String userId, List<Permission> permissions) {
		Response<List<Permission>> response = http.exchange(HttpMethod.POST, http.uri(scope, user(userId)), permissions,
				new ParameterizedTypeReference<List<Permission>>() {}, Set.of(200, 201, 206), true);
		
		return new BatchResult<>(response.getBody(), response.getStatus(), response.getStatus() == 206);
	}

	/**
	 * Sends a permission-retrieval request for the specified resource scope.
	 *
	 * @param scope  endpoint path identifying the resource scope
	 * @param userId ID of the user whose permissions are retrieved
	 * @return permissions assigned to the user within the selected scope
	 */
	private List<Permission> get(String[] scope, String userId) {
		return http.exchange(HttpMethod.GET, http.uri(scope, user(userId)), null,
				new ParameterizedTypeReference<List<Permission>>() {}, Set.of(200), true).getBody();
	}

	/**
	 * Sends a permission-update request for the specified resource scope.
	 *
	 * @param scope endpoint path identifying the resource scope
	 * @param userId ID of the user whose permissions are updated
	 * @param permissions the desired permissions
	 * @return the added and removed actions
	 * @throws PermissionsRejectedException when the update adds actions the requester may not grant
	 */
	private PermissionReplacement update(String[] scope, String userId, List<Permission> permissions) {
		try {
			return http.exchange(HttpMethod.PUT, http.uri(scope, user(userId)), permissions, PermissionReplacement.class, Set.of(200), true).getBody();
		} catch (TrustDeckResponseException exception) {
			// A 403 without rejected actions means the requester may not manage these permissions at all
			if (exception.getStatusCode() != 403) {
				throw exception;
			}

			// A 403 with rejected actions lists what the requester may not grant
			PermissionReplacement replacement;
			try {
				replacement = http.mapper().readValue(exception.getRawBody(), PermissionReplacement.class);
			} catch (Exception parseFailure) {
				throw exception;
			}

			if (replacement == null || replacement.getRejected() == null || replacement.getRejected().isEmpty()) {
				throw exception;
			}

			throw new PermissionsRejectedException(exception.getResponseStatusCode(), exception.getStatusInfo(),
					exception.getRawBody(), exception.getContentType(), replacement);
		}
	}

	/**
	 * Sends a permission-deletion request for the specified resource scope.
	 *
	 * @param scope endpoint path identifying the resource scope
	 * @param userId ID of the user whose permissions are deleted
	 * @param permissions permissions to delete
	 * @return result containing the deletion outcomes and response status
	 */
	private BatchResult<Boolean> delete(String[] scope, String userId, List<Permission> permissions) {
		Response<List<Boolean>> response = http.exchange(HttpMethod.DELETE, http.uri(scope, user(userId)), permissions,
				new ParameterizedTypeReference<List<Boolean>>() {}, Set.of(204, 206), true);
		
		return new BatchResult<>(response.getBody() == null ? List.of() : response.getBody(), response.getStatus(),
				response.getStatus() == 206);
	}

	/**
	 * Builds the user query parameter for a permission request.
	 *
	 * @param userId ID of the affected user
	 * @return query parameters containing the validated user ID
	 */
	private static Map<String, Object> user(String userId) {
		return Map.of("userId", TrustDeckHttpClient.require(userId, "userId"));
	}
	
	/**
	 * Builds a permission endpoint path and validates each appended segment.
	 *
	 * @param suffix additional permission path segments
	 * @return the complete permission endpoint path segments
	 * @throws IllegalArgumentException if a supplied path segment is {@code null} or blank
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
