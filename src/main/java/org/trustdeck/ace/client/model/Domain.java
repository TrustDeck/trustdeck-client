/*
 * ACE (Advanced Confidentiality Engine) Client Library
 * Copyright 2025 Chethan Chinnabhandara Nagaraj & Armin Müller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustdeck.ace.client.model;

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
public class Domain {
    /** Unique identifier of the domain */
    private Integer id;

    /** Name of the domain */
    private String name;

    /** Prefix used for pseudonyms in this domain */
    private String prefix;

    /** Start of domain validity period */
    private LocalDateTime validFrom;

    /** Whether validFrom is inherited from parent domain */
    private Boolean validFromInherited;

    /** End of domain validity period */
    private LocalDateTime validTo;

    /** Validity period as a string (e.g., "1d") */
    private String validityTime;

    /** Whether validTo is inherited from parent domain */
    private Boolean validToInherited;

    /** Enforce start date validity for pseudonyms */
    private Boolean enforceStartDateValidity;

    /** Whether enforceStartDateValidity is inherited */
    private Boolean enforceStartDateValidityInherited;

    /** Enforce end date validity for pseudonyms */
    private Boolean enforceEndDateValidity;

    /** Whether enforceEndDateValidity is inherited */
    private Boolean enforceEndDateValidityInherited;

    /** Algorithm used for pseudonymization */
    private String algorithm;

    /** Whether algorithm is inherited from parent domain */
    private Boolean algorithmInherited;

    /** Alphabet used for pseudonym generation */
    private String alphabet;

    /** Whether alphabet is inherited from parent domain */
    private Boolean alphabetInherited;

    /** Desired size for random algorithm */
    private Long randomAlgorithmDesiredSize;

    /** Whether randomAlgorithmDesiredSize is inherited */
    private Boolean randomAlgorithmDesiredSizeInherited;

    /** Desired success probability for random algorithm */
    private Double randomAlgorithmDesiredSuccessProbability;

    /** Whether randomAlgorithmDesiredSuccessProbability is inherited */
    private Boolean randomAlgorithmDesiredSuccessProbabilityInherited;

    /** Whether multiple pseudonyms are allowed per identifier */
    private Boolean multiplePsnAllowed;

    /** Whether multiplePsnAllowed is inherited */
    private Boolean multiplePsnAllowedInherited;

    /** Counter for consecutive pseudonym values */
    private Long consecutiveValueCounter;

    /** Length of the pseudonym */
    private Integer pseudonymLength;

    /** Whether pseudonymLength is inherited */
    private Boolean pseudonymLengthInherited;

    /** Character used for padding pseudonyms */
    private Character paddingCharacter;

    /** Whether paddingCharacter is inherited */
    private Boolean paddingCharacterInherited;

    /** Whether to add a check digit to pseudonyms */
    private Boolean addCheckDigit;

    /** Whether addCheckDigit is inherited */
    private Boolean addCheckDigitInherited;

    /** Whether length includes the check digit */
    private Boolean lengthIncludesCheckDigit;

    /** Whether lengthIncludesCheckDigit is inherited */
    private Boolean lengthIncludesCheckDigitInherited;

    /** Salt value for pseudonymization */
    private String salt;

    /** Length of the salt */
    private Integer saltLength;

    /** Description of the domain */
    private String description;

    /** ID of the parent (super) domain */
    private Integer superDomainID;

    /** Name of the parent (super) domain */
    private String superDomainName;

    @Override
    public String toString() {
        return "Domain{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                '}';
    }
}