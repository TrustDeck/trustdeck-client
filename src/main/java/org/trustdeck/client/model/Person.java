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
import lombok.NoArgsConstructor;

/**
 * The Data Transfer Object for the Person object for interacting with TrustDeck.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Person {
	
	/** The (internal) id of the person object. */
    private Integer id;
	
	/** the first name(s) of the person. */
    private String firstName;
	
	/** The person's last name. */
    private String lastName;
	
	/** The last name the person had at birth (e.g. before marriage). */
    private String birthName;
	
	/** The administrative gender of the person. */
    private String administrativeGender;
	
	/** The person's date of birth. */
    private String dateOfBirth;
	
	/** The street name and number of the person's address. */
    private String street;
	
	/** The postal code of the person's address. */
    private String postalCode;
	
	/** The city name of the person's address. */
    private String city;
	
	/** The country name of the person's address. */
    private String country;
	
	/** The identifier for the person. */
    private String identifier;
	
	/** The identifier's type. */
    private String idType;
	
	/** The algorithm that should be used to create an identifier. */
    private Algorithm algorithm;
}
