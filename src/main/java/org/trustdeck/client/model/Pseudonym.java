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
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Data Transfer Object (DTO) for pseudonymization records in TrustDeck.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pseudonym {
	
    /** Identifier of the record. */
    @NotBlank
    private String id;

    /** Type of the identifier. */
    @NotBlank
    private String idType;

    /** Pseudonym value. */
    private String psn;

    /** Start of validity period. */
    private Timestamp validFrom;

    /** Whether validFrom is inherited from domain. */
    private Boolean validFromInherited;

    /** End of validity period. */
    private Timestamp validTo;

    /** Whether validTo is inherited from domain. */
    private Boolean validToInherited;

    /** Validity period as a string (e.g., "1d"). */
    private String validityTime;

    /** Name of the domain. */
    private String domainName;
}