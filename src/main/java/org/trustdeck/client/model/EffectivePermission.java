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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * An action effective for a resource.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
public class EffectivePermission {
	
	/** Creates an empty permission model for JSON binding. */
	public EffectivePermission() { }

	/** Resource type. */
	private String resourceType;
	
	/** Resource name. */
	private String resourceName;
	
	/** Project abbreviation, when applicable. */
	private String projectAbbreviation;
	
	/** Allowed action. */
	private String action;
	
	/** Permission validity start. */
	private OffsetDateTime validFrom;
	
	/** Permission validity end. */
	private OffsetDateTime validTo;
}
