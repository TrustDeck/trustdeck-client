package org.trustdeck.client.model;

import java.time.OffsetDateTime;

import lombok.Data;

/** An action effective for a resource. */
@Data
public class EffectivePermission {
	/** Creates an empty permission model for JSON binding. */
	public EffectivePermission() { }
	private String resourceType;
	private String resourceName;
	private String projectAbbreviation;
	private String action;
	private OffsetDateTime validFrom;
	private OffsetDateTime validTo;
}
