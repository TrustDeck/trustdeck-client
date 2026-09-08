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

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A generic project entity.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entity {
	
	/** Creates an empty entity model for JSON binding. */
	public Entity() { }

	/** Database identifier. */
	private Long id;
	
	/** Stable TrustDeck identifier. */
	@JsonProperty("trustdeckID")
	private UUID trustdeckID;
	
	/** Owning project identifier. */
	private Integer projectID;
	
	/** Owning project name. */
	private String projectName;
	
	/** Entity-type identifier. */
	private Integer entityTypeID;
	
	/** Entity-type name. */
	private String entityTypeName;
	
	/** Arbitrary entity payload. */
	private JsonNode data;
	
	/** Whether the entity is deleted. */
	private Boolean isDeleted;
	
	/** Creation timestamp. */
	private OffsetDateTime createdAt;
	
	/** Last-update timestamp. */
	private OffsetDateTime updatedAt;
}
