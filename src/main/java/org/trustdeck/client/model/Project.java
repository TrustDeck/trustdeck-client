package org.trustdeck.client.model;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/** A TrustDeck project. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project {
	/** Creates an empty project model for JSON binding. */
	public Project() { }
	private Integer id;
	private String name;
	private String abbreviation;
	private OffsetDateTime startDate;
	private OffsetDateTime endDate;
	private Boolean storeEntities;
	private Boolean storePseudonyms;
	private String description;
}
