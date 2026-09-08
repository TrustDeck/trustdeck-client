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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A base or project-scoped entity type.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityType {
	
	/** Creates an empty entity-type model for JSON binding. */
	public EntityType() { }

	/** Type identifier. */
	private Integer id;
	
	/** Type name. */
	private String name;
	
	/** Type version. */
	private String version;
	
	/** Whether this type is deprecated. */
	private Boolean isDeprecated;
	
	/** Whether this is a base type. */
	private Boolean isBaseType;
	
	/** JSON type definition. */
	private JsonNode typeDefinition;
	
	/** Name of the base type. */
	private String baseTypeName;
	
	/** Identifier of the base type. */
	private Integer baseTypeId;
	
	/** Associated domain name. */
	private String associatedDomainName;
	
	/** Associated project name. */
	private String projectName;
	
	/** Associated project identifier. */
	private Integer projectId;
}
