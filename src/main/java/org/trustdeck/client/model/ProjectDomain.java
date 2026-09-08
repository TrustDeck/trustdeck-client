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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A concise domain view owned by a project.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDomain {
	
	/** Creates an empty project-domain model for JSON binding. */
	public ProjectDomain() { }
	
	/** Domain name. */
	private String name;
	
	/** Domain prefix. */
	private String prefix;
	
	/** Owning project abbreviation. */
	private String projectAbbreviation;
	
	/** Parent domain name. */
	private String superDomainName;
}
