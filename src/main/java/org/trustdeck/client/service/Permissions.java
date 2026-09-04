package org.trustdeck.client.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.BatchResult;
import org.trustdeck.client.model.EffectivePermission;
import org.trustdeck.client.model.Permission;
import org.trustdeck.client.model.SearchResult;
import org.trustdeck.client.model.User;

/** Permission and user-management operations. */
public class Permissions {
	private final TrustDeckHttpClient http;
	/** Creates the permission service.
	 * @param http shared HTTP transport
	 */
	public Permissions(TrustDeckHttpClient http) { this.http = http; }
	/** Searches users and exposes HTTP 206 as partial.
	 * @param query search query
	 * @return user search result
	 */
	public SearchResult<User> searchUsers(String query) { var r = http.exchange(HttpMethod.GET, http.uri(path("users"), Map.of("query", TrustDeckHttpClient.require(query, "query"))), null, new ParameterizedTypeReference<List<User>>() {}, Set.of(200, 206), true); return new SearchResult<>(r.getBody(), r.getStatus() == 206); }
	/** Creates domain permissions.
	 * @param domain domain name
	 * @param user subject identifier
	 * @param values permissions
	 * @return batch result
	 */
	public BatchResult<Permission> createDomain(String domain, String user, List<Permission> values) { return create(path("domains", domain), user, values); }
	/** Creates project permissions.
	 * @param project project abbreviation
	 * @param user subject identifier
	 * @param values permissions
	 * @return batch result
	 */
	public BatchResult<Permission> createProject(String project, String user, List<Permission> values) { return create(path("projects", project), user, values); }
	/** Creates global permissions.
	 * @param user subject identifier
	 * @param values permissions
	 * @return batch result
	 */
	public BatchResult<Permission> createGlobal(String user, List<Permission> values) { return create(path("global"), user, values); }
	/** Creates entity-type permissions.
	 * @param project project abbreviation
	 * @param type entity type name
	 * @param user subject identifier
	 * @param values permissions
	 * @return batch result
	 */
	public BatchResult<Permission> createEntityType(String project, String type, String user, List<Permission> values) { return create(path("projects", project, "entity-types", type), user, values); }
	/** Gets domain permissions.
	 * @param domain domain name
	 * @param user subject identifier
	 * @return permissions
	 */
	public List<Permission> getDomain(String domain, String user) { return get(path("domains", domain), user); }
	/** Gets project permissions.
	 * @param project project abbreviation
	 * @param user subject identifier
	 * @return permissions
	 */
	public List<Permission> getProject(String project, String user) { return get(path("projects", project), user); }
	/** Gets global permissions.
	 * @param user subject identifier
	 * @return permissions
	 */
	public List<Permission> getGlobal(String user) { return get(path("global"), user); }
	/** Gets entity-type permissions.
	 * @param project project abbreviation
	 * @param type entity type name
	 * @param user subject identifier
	 * @return permissions
	 */
	public List<Permission> getEntityType(String project, String type, String user) { return get(path("projects", project, "entity-types", type), user); }
	/** Gets permissions available to the current user.
	 * @return available permissions
	 */
	public List<EffectivePermission> getAvailable() { return http.exchange(HttpMethod.GET, http.uri(path(), Map.of()), null, new ParameterizedTypeReference<List<EffectivePermission>>() {}, Set.of(200), true).getBody(); }
	/** Updates domain permissions.
	 * @param domain domain name
	 * @param user subject identifier
	 * @param values permissions
	 * @return {@code true} after HTTP 200
	 */
	public boolean updateDomain(String domain, String user, List<Permission> values) { return update(path("domains", domain), user, values); }
	/** Updates project permissions.
	 * @param project project abbreviation
	 * @param user subject identifier
	 * @param values permissions
	 * @return {@code true} after HTTP 200
	 */
	public boolean updateProject(String project, String user, List<Permission> values) { return update(path("projects", project), user, values); }
	/** Updates global permissions.
	 * @param user subject identifier
	 * @param values permissions
	 * @return {@code true} after HTTP 200
	 */
	public boolean updateGlobal(String user, List<Permission> values) { return update(path("global"), user, values); }
	/** Updates entity-type permissions.
	 * @param project project abbreviation
	 * @param type entity type name
	 * @param user subject identifier
	 * @param values permissions
	 * @return {@code true} after HTTP 200
	 */
	public boolean updateEntityType(String project, String type, String user, List<Permission> values) { return update(path("projects", project, "entity-types", type), user, values); }
	/** Deletes domain permissions and preserves batch status.
	 * @param domain domain name
	 * @param user subject identifier
	 * @param values permissions
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteDomain(String domain, String user, List<Permission> values) { return delete(path("domains", domain), user, values); }
	/** Deletes project permissions.
	 * @param project project abbreviation
	 * @param user subject identifier
	 * @param values permissions
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteProject(String project, String user, List<Permission> values) { return delete(path("projects", project), user, values); }
	/** Deletes global permissions.
	 * @param user subject identifier
	 * @param values permissions
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteGlobal(String user, List<Permission> values) { return delete(path("global"), user, values); }
	/** Deletes entity-type permissions.
	 * @param project project abbreviation
	 * @param type entity type name
	 * @param user subject identifier
	 * @param values permissions
	 * @return batch result
	 */
	public BatchResult<Boolean> deleteEntityType(String project, String type, String user, List<Permission> values) { return delete(path("projects", project, "entity-types", type), user, values); }
	private BatchResult<Permission> create(String[] path, String user, List<Permission> values) { var r = http.exchange(HttpMethod.POST, http.uri(path, user(user)), values, new ParameterizedTypeReference<List<Permission>>() {}, Set.of(200, 201, 206), true); return new BatchResult<>(r.getBody(), r.getStatus(), r.getStatus() == 206); }
	private List<Permission> get(String[] path, String user) { return http.exchange(HttpMethod.GET, http.uri(path, user(user)), null, new ParameterizedTypeReference<List<Permission>>() {}, Set.of(200), true).getBody(); }
	private boolean update(String[] path, String user, List<Permission> values) { http.empty(HttpMethod.PUT, http.uri(path, user(user)), values, Set.of(200), true); return true; }
	private BatchResult<Boolean> delete(String[] path, String user, List<Permission> values) { var r = http.exchange(HttpMethod.DELETE, http.uri(path, user(user)), values, new ParameterizedTypeReference<List<Boolean>>() {}, Set.of(204, 206), true); return new BatchResult<>(r.getBody() == null ? List.of() : r.getBody(), r.getStatus(), r.getStatus() == 206); }
	private static Map<String, Object> user(String value) { return Map.of("userId", TrustDeckHttpClient.require(value, "userId")); }
	private static String[] path(String... suffix) { String[] path = new String[2 + suffix.length]; path[0] = "api"; path[1] = "permissions"; for (int i = 0; i < suffix.length; i++) path[i + 2] = TrustDeckHttpClient.require(suffix[i], "path value"); return path; }
}
