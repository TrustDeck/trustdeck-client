package org.trustdeck.client.model;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/** A permission grant. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission {
	/** Creates an empty permission model for JSON binding. */
	public Permission() { }
	private Integer id;
	private String subjectId;
	private String resourceType;
	private Integer resourceId;
	private String domainName;
	private String projectAbbreviation;
	private String entityTypeName;
	private String action;
	private String decision;
	private OffsetDateTime validFrom;
	private OffsetDateTime validTo;
	private OffsetDateTime createdAt;
	private String createdBy;
	private OffsetDateTime updatedAt;
	private String updatedBy;
}
