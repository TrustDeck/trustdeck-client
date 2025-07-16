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

package org.trustdeck.ace.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Data Transfer Object (DTO) for algorithms in TrustDeck.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Algorithm {

	/** The (internal) identifier of the algorithm. */
    private Integer id;

	/** The name of the algorithm. */
    private String name;

	/** The alphabet used in the algorithm. */
    private String alphabet;

	/** If it's a randomness-based algorithm: how many pseudonyms/identifiers should the algorithm be able to create? */
    private long randomAlgorithmDesiredSize;

	/** If it's a randomness-based algorithm: which which probability should the creation of a pseudonym/identifier be successful? */
    private double randomAlgorithmDesiredSuccessProbability;

	/** If a counter-based approach is used: the value of the counter. */
    private long consecutiveValueCounter;

	/** The length of the pseudonym/identifier. */
    private int pseudonymLength;

	/** The character used for padding to the desired length. */
    private String paddingCharacter;

	/** Should a check digit be added to the pseudonym/identifier? */
    private boolean addCheckDigit;

	/** Should the given pseudonym-length include the check digit, or should the check digit just be appended. */
    private boolean lengthIncludesCheckDigit;

	/** The salt value for this algorithm. */
    private String salt;

	/** The length of the salt-value. */
    private int saltLength;
}
