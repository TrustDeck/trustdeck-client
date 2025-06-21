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
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * Data Transfer Object (DTO) for pseudonymization records, matching the pseudonymization service's data model.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PseudonymDto {
    @NotBlank
    private String id; // Identifier of the record
    @NotBlank
    private String idType; // Type of the identifier
    private String psn; // Pseudonym value
    private Timestamp validFrom; // Start of validity period
    private Boolean validFromInherited; // Whether validFrom is inherited from domain
    private Timestamp validTo; // End of validity period
    private Boolean validToInherited; // Whether validTo is inherited from domain
    private String validityTime; // Validity period as a string (e.g., "1d")
    private String domainName; // Name of the domain


    @Override
    public String toString() {
        return "PseudonymisationDto{" +
                "id=" + id +
                ", idType='" + idType + '\'' +
                ", psn='" + psn + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", domainName=" + domainName +
                '}';
    }
}