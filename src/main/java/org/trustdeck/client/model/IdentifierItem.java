/*
 * Trust Deck Client Library
 * Copyright 2025 TrustDeck Team
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * IdentifierItem object for TrustDeck.
 * Encapsulates the actual identifier and its type.
 * 
 * @author Armin Müller
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IdentifierItem {
	
	/** The identifying string. */
	private String identifier;
	
	/** The type of the identifier (e.g. social security number, or statutory health insurance number, ...). */
	private String idType;
}
