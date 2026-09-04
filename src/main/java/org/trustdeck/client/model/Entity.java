package org.trustdeck.client.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

/** A generic project entity. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entity {
	/** Creates an empty entity model for JSON binding. */
	public Entity() { }
	private Long id;
	@JsonProperty("trustdeckID")
	private UUID trustdeckID;
	private Integer projectID;
	private String projectName;
	private Integer entityTypeID;
	private String entityTypeName;
	private JsonNode data;
	private Boolean isDeleted;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
