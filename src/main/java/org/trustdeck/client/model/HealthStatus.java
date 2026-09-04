package org.trustdeck.client.model;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/** Health information reported by TrustDeck. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthStatus {
	/** Creates an empty health model for JSON binding. */
	public HealthStatus() { }
	private String status;
	private String service;
	private OffsetDateTime timestamp;
}
