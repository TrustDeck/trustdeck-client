/*
 *  * Copyright 2024 Your Organization
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */

package org.trustdeck.ace.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for domains in the pseudonymization service.
 */

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainDto {
    private Integer id; // Unique identifier of the domain
    private String name; // Name of the domain
    private String prefix; // Prefix used for pseudonyms in this domain
    private LocalDateTime validFrom; // Start of domain validity period
    private Boolean validFromInherited; // Whether validFrom is inherited from parent domain
    private LocalDateTime validTo; // End of domain validity period
    private String validityTime; // Validity period as a string (e.g., "1d")
    private Boolean validToInherited; // Whether validTo is inherited from parent domain
    private Boolean enforceStartDateValidity; // Enforce start date validity for pseudonyms
    private Boolean enforceStartDateValidityInherited; // Whether enforceStartDateValidity is inherited
    private Boolean enforceEndDateValidity; // Enforce end date validity for pseudonyms
    private Boolean enforceEndDateValidityInherited; // Whether enforceEndDateValidity is inherited
    private String algorithm; // Algorithm used for pseudonymization
    private Boolean algorithmInherited; // Whether algorithm is inherited from parent domain
    private String alphabet; // Alphabet used for pseudonym generation
    private Boolean alphabetInherited; // Whether alphabet is inherited from parent domain
    private Long randomAlgorithmDesiredSize; // Desired size for random algorithm
    private Boolean randomAlgorithmDesiredSizeInherited; // Whether randomAlgorithmDesiredSize is inherited
    private Double randomAlgorithmDesiredSuccessProbability; // Desired success probability for random algorithm
    private Boolean randomAlgorithmDesiredSuccessProbabilityInherited; // Whether randomAlgorithmDesiredSuccessProbability is inherited
    private Boolean multiplePsnAllowed; // Whether multiple pseudonyms are allowed per identifier
    private Boolean multiplePsnAllowedInherited; // Whether multiplePsnAllowed is inherited
    private Long consecutiveValueCounter; // Counter for consecutive pseudonym values
    private Integer pseudonymLength; // Length of the pseudonym
    private Boolean pseudonymLengthInherited; // Whether pseudonymLength is inherited
    private Character paddingCharacter; // Character used for padding pseudonyms
    private Boolean paddingCharacterInherited; // Whether paddingCharacter is inherited
    private Boolean addCheckDigit; // Whether to add a check digit to pseudonyms
    private Boolean addCheckDigitInherited; // Whether addCheckDigit is inherited
    private Boolean lengthIncludesCheckDigit; // Whether length includes the check digit
    private Boolean lengthIncludesCheckDigitInherited; // Whether lengthIncludesCheckDigit is inherited
    private String salt; // Salt value for pseudonymization
    private Integer saltLength; // Length of the salt
    private String description; // Description of the domain
    private Integer superDomainID; // ID of the parent (super) domain
    private String superDomainName; // Name of the parent (super) domain

    @Override
    public String toString() {
        return "DomainDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                '}';
    }
}