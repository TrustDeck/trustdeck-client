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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.trustdeck.client.config.TrustDeckTokenService;

/**
 * Class to provide helper methods.
 * 
 * @author Chethan Nagaraj, Armin Müller
 */
@Component
public class TrustDeckClientUtil {
	
	/** The service that handles authentication and token creation. */
	@Autowired
	private TrustDeckTokenService tokenService;
    
	/**
	 * Helper method to create the correct HTTP headers.
	 * 
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
}
