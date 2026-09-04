package org.trustdeck.client.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/** Update payload for a pseudonym record. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PseudonymUpdate {
	/** Creates an empty update model for JSON binding. */
	public PseudonymUpdate() { }
	private IdentifierItem oldIdentifierItem;
	private String oldPsn;
	private IdentifierItem newIdentifierItem;
	private String newPsn;
	private LocalDateTime validFrom;
	private Boolean validFromInherited;
	private LocalDateTime validTo;
	private Boolean validToInherited;
	private String validityTime;
	private String newDomainName;
}
