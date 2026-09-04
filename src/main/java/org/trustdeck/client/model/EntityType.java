package org.trustdeck.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

/** A base or project-scoped entity type. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityType {
	/** Creates an empty entity-type model for JSON binding. */
	public EntityType() { }
	private Integer id;
	private String name;
	private String version;
	private Boolean isDeprecated;
	private Boolean isBaseType;
	private JsonNode typeDefinition;
	private String baseTypeName;
	private Integer baseTypeId;
	private String associatedDomainName;
	private String projectName;
	private Integer projectId;
}
