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
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.BatchResult;
import org.trustdeck.client.model.Permission;

/**
 * Provides permission operations scoped to a selected TrustDeck domain.
 * No ownership lookup is performed.
 *  
 * @author Armin Müller
 */
public final class DomainPermissions {

	/** Permission service used to execute the scoped operations. */
	private final Permissions permissions;

	/** Name of the domain represented by this permission scope. */
	private final String domain;

	/**
	 * Creates domain permission operations.
	 * 
	 * @param permissions shared permission service
	 * @param domain selected domain name
	 */
	public DomainPermissions(Permissions permissions, String domain) {
		this.permissions = permissions;
		this.domain = TrustDeckHttpClient.require(domain, "domainName");
	}

	/**
	 * Creates permissions for a user.
	 * 
	 * @param userId subject identifier
	 * @param values permissions to create
	 * @return created permissions
	 */
	public BatchResult<Permission> create(String userId, List<Permission> values) {
		return permissions.createScoped(path(), userId, values);
	}

	/**
	 * Gets permissions for a user.
	 * 
	 * @param userId subject identifier
	 * @return permissions
	 */
	public List<Permission> get(String userId) {
		return permissions.getScoped(path(), userId);
	}

	/**
	 * Updates permissions for a user.
	 * 
	 * @param userId subject identifier
	 * @param values replacement permissions
	 * @return {@code true} when accepted
	 */
	public boolean update(String userId, List<Permission> values) {
		return permissions.updateScoped(path(), userId, values);
	}

	/**
	 * Deletes permissions for a user.
	 * 
	 * @param userId subject identifier
	 * @param values permissions to delete
	 * @return deletion results
	 */
	public BatchResult<Boolean> delete(String userId, List<Permission> values) {
		return permissions.deleteScoped(path(), userId, values);
	}

	/**
	 * Builds the endpoint path for permissions on the selected domain.
	 *
	 * @return the domain-permission endpoint path segments
	 */
	private String[] path() {
		return new String[] { "api", "permissions", "domains", domain };
	}
}
