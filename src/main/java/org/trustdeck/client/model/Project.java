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

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A TrustDeck project.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project {
	
	/** Creates an empty project model for JSON binding. */
	public Project() { }

	/** Project identifier. */
	private Integer id;
	
	/** Project name. */
	private String name;
	
	/** Project abbreviation. */
	private String abbreviation;
	
	/** Project start date. */
	private OffsetDateTime startDate;
	
	/** Project end date. */
	private OffsetDateTime endDate;
	
	/** Whether entities are stored. */
	private Boolean storeEntities;
	
	/** Whether pseudonyms are stored. */
	private Boolean storePseudonyms;
	
	/** Project description. */
	private String description;
}
