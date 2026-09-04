package org.trustdeck.client.model;

import java.util.List;

import lombok.Data;

/** A TrustDeck user returned by permission search. */
@Data
public class User {
	/** Creates an empty user model for JSON binding. */
	public User() { }
	private String userId;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private List<String> keycloakRoles;
	private List<EffectivePermission> effectivePermissions;
	private String federationProviderName;
	private String federationProviderId;
}
