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
 * A permission grant.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission {
	
	/** Creates an empty permission model for JSON binding. */
	public Permission() { }

	/** Permission identifier. */
	private Integer id;
	
	/** Permission subject identifier. */
	private String subjectId;
	
	/** Protected resource type. */
	private String resourceType;
	
	/** Protected resource identifier. */
	private Integer resourceId;
	
	/** Protected domain name. */
	private String domainName;
	
	/** Project abbreviation, when applicable. */
	private String projectAbbreviation;
	
	/** Entity-type name, when applicable. */
	private String entityTypeName;
	
	/** Requested action. */
	private String action;
	
	/** Permission decision. */
	private String decision;
	
	/** Permission validity start. */
	private OffsetDateTime validFrom;
	
	/** Permission validity end. */
	private OffsetDateTime validTo;
	
	/** Creation timestamp. */
	private OffsetDateTime createdAt;
	
	/** Creator identifier. */
	private String createdBy;
	
	/** Last-update timestamp. */
	private OffsetDateTime updatedAt;
	
	/** Last updater identifier. */
	private String updatedBy;
}
