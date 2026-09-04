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

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for domains in TrustDeck.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Builder
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Domain {
	/** Creates an empty domain model for JSON binding. */
	public Domain() { }
	
    /** Unique identifier of the domain. */
    private Integer id;

    /** Name of the domain. */
    private String name;

    /** Prefix used for pseudonyms in this domain. */
    private String prefix;

    /** Start of domain validity period. */
    private LocalDateTime validFrom;

    /** Whether validFrom is inherited from parent domain. */
    private Boolean validFromInherited;

    /** End of domain validity period. */
    private LocalDateTime validTo;

    /** Validity period as a string (e.g., "1d"). */
    private String validityTime;

    /** Whether validTo is inherited from parent domain. */
    private Boolean validToInherited;

    /** Enforce start date validity for pseudonyms. */
    private Boolean enforceStartDateValidity;

    /** Whether enforceStartDateValidity is inherited. */
    private Boolean enforceStartDateValidityInherited;

    /** Enforce end date validity for pseudonyms. */
    private Boolean enforceEndDateValidity;

    /** Whether enforceEndDateValidity is inherited. */
    private Boolean enforceEndDateValidityInherited;

    /** Algorithm used for pseudonymization. */
    private Algorithm algorithm;

    /** Whether algorithm is inherited from parent domain. */
    private Boolean algorithmInherited;

    /** Whether multiple pseudonyms are allowed per identifier. */
    private Boolean multiplePsnAllowed;

    /** Whether multiplePsnAllowed is inherited. */
    private Boolean multiplePsnAllowedInherited;

    /** Description of the domain. */
    private String description;

    /** ID of the parent (super) domain. */
    private Integer superDomainID;

    /** Name of the parent (super) domain. */
    private String superDomainName;

    /** The abbreviation of the owning project. */
    private String projectAbbreviation;
}
