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

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Update payload for a pseudonym record.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PseudonymUpdate {
	
	/** Creates an empty update model for JSON binding. */
	public PseudonymUpdate() { }

	/** Previous identifier item. */
	private IdentifierItem oldIdentifierItem;
	
	/** Previous pseudonym value. */
	private String oldPsn;
	
	/** Replacement identifier item. */
	private IdentifierItem newIdentifierItem;
	
	/** Replacement pseudonym value. */
	private String newPsn;
	
	/** Validity start. */
	private LocalDateTime validFrom;
	
	/** Whether the validity start is inherited. */
	private Boolean validFromInherited;
	
	/** Validity end. */
	private LocalDateTime validTo;
	
	/** Whether the validity end is inherited. */
	private Boolean validToInherited;
	
	/** Validity-time representation used by the API. */
	private String validityTime;
	
	/** Replacement domain name. */
	private String newDomainName;
}
