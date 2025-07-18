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

package org.trustdeck.client.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.service.TrustDeckTokenService;

/**
 * Class to provide helper methods.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
public class TrustDeckClientUtil {
	
	private TrustDeckTokenService tokenService;
	
	public TrustDeckClientUtil(TrustDeckTokenService tokenService) {
		this.tokenService = tokenService;
	}
	
	/**
	 * Helper method to create the correct HTTP headers.
	 * 
	 * @param tokenService service that handles authentication and token creation
	 * @return a HTTP entity object containing the necessary headers.
	 */
    public HttpEntity<?> createRequestEntity() {
    	// Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenService.authenticate());
        
        // Build request entity
        return new HttpEntity<>(headers);
    }
    
	/**
	 * Helper method to create the correct HTTP headers and to add a body.
	 * 
	 * @param tokenService service that handles authentication and token creation
	 * @param <T> the type of the body object
	 * @param body the body for this request entity
	 * @return a HTTP entity object containing the necessary headers and the body.
	 */
    public <T> HttpEntity<?> createRequestEntity(T body) {
    	// Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenService.authenticate());
        
        // Build request entity
        return new HttpEntity<>(body, headers);
    }
    
    /**
     * Helper method that extracts an attribute out of a domain.
     * 
     * @param domain the domain containing the attribute of interest
     * @param attributeName the attribute of interest
     * @return the attribute as a String
     */
    public String extractAttributeOutOfDomain(Domain domain, String attributeName) {
    	// Get required attribute
    	switch (attributeName.trim().toLowerCase()) {
		case "id": {
			return domain.getId().toString();
		} case "name": {
			return domain.getName();
		} case "prefix": {
			return domain.getPrefix();
		} case "validfrom": {
			return domain.getValidFrom().toString();
		} case "validfrominherited": {
			return domain.getValidFromInherited().toString();
		} case "validto": {
			return domain.getValidTo().toString();
		} case "validtoinherited": {
			return domain.getValidToInherited().toString();
		} case "enforcestartdatevalidity": {
			return domain.getEnforceStartDateValidity().toString();
		} case "enforcestartdatevalidityinherited": {
			return domain.getEnforceStartDateValidityInherited().toString();
		} case "enforceenddatevalidity": {
			return domain.getEnforceEndDateValidity().toString();
		} case "enforceenddatevalidityinherited": {
			return domain.getEnforceEndDateValidityInherited().toString();
		} case "algorithm": {
			return domain.getAlgorithm();
		} case "algorithminherited": {
			return domain.getAlgorithmInherited().toString();
		} case "alphabet": {
			return domain.getAlphabet();
		} case "alphabetinherited": {
			return domain.getAlphabetInherited().toString();
		} case "randomalgorithmdesiredsize": {
			return domain.getRandomAlgorithmDesiredSize().toString();
		} case "randomalgorithmdesiredsizeinherited": {
			return domain.getRandomAlgorithmDesiredSizeInherited().toString();
		} case "randomalgorithmdesiredsuccessprobability": {
			return domain.getRandomAlgorithmDesiredSuccessProbability().toString();
		} case "randomalgorithmdesiredsuccessprobabilityinherited": {
			return domain.getRandomAlgorithmDesiredSuccessProbabilityInherited().toString();
		} case "multiplepsnallowed": {
			return domain.getMultiplePsnAllowed().toString();
		} case "multiplepsnallowedinherited": {
			return domain.getMultiplePsnAllowedInherited().toString();
		} case "consecutivevaluecounter": {
			return domain.getConsecutiveValueCounter().toString();
		} case "pseudonymlength": {
			return domain.getPseudonymLength().toString();
		} case "pseudonymlengthinherited": {
			return domain.getPseudonymLengthInherited().toString();
		} case "paddingcharacter": {
			return domain.getPaddingCharacter().toString();
		} case "paddingcharacterinherited": {
			return domain.getPaddingCharacterInherited().toString();
		} case "addcheckdigit": {
			return domain.getAddCheckDigit().toString();
		} case "addcheckdigitinherited": {
			return domain.getAddCheckDigitInherited().toString();
		} case "lengthincludescheckdigit": {
			return domain.getLengthIncludesCheckDigit().toString();
		} case "lengthincludescheckdigitinherited": {
			return domain.getLengthIncludesCheckDigitInherited().toString();
		} case "salt": {
			return domain.getSalt();
		} case "saltlength": {
			return domain.getSaltLength().toString();
		} case "description": {
			return domain.getDescription();
		} case "superdomainid": {
			return domain.getSuperDomainID().toString();
		} default:
			// The requested attribute was not found.
			return null;
		}
    }
}
