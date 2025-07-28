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

package org.trustdeck.client.exception;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.Setter;

/**
 * Exception to encapsulate non-desired responses of the TrustDeck.
 * 
 * @author Armin Müller
 */
public class TrustDeckResponseException extends RuntimeException {

	/** Exception UID. */
	private static final long serialVersionUID = 890444675247657376L;
	
	/** The status code originally returned by the triggering request. */
	@Getter
	@Setter
	private HttpStatusCode responseStatusCode;
	
	/** 
	 * Constructor that also defines the triggering status code and a message.
	 * 
	 * @param message a message explaining the cause of this exception
	 * @param responseStatusCode the status code of the response of the exception-causing request
	 * 
	 */
	public TrustDeckResponseException(String message, HttpStatusCode responseStatusCode) {
		super(message);
		this.responseStatusCode = responseStatusCode;
	}
}
