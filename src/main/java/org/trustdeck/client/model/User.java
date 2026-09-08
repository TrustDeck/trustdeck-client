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

package org.trustdeck.client.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A TrustDeck user returned by permission search.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
public class User {
	
	/** Creates an empty user model for JSON binding. */
	public User() { }

	/** Keycloak user identifier. */
	private String userId;
	
	/** User name. */
	private String username;
	
	/** First name. */
	private String firstName;
	
	/** Last name. */
	private String lastName;
	
	/** Email address. */
	private String email;
	
	/** Keycloak roles. */
	private List<String> keycloakRoles;
	
	/** Permissions effective for this user. */
	private List<EffectivePermission> effectivePermissions;
	
	/** Federation provider name. */
	private String federationProviderName;
	
	/** Federation provider identifier. */
	private String federationProviderId;
}
