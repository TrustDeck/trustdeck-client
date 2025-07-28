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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception to represent non-recoverable states of the client library.
 * 
 * @author Armin Müller
 */
@Slf4j
public class TrustDeckClientLibraryException extends RuntimeException {

	/** Exception UID. */
	private static final long serialVersionUID = -3008995479625356765L;
	
	/** The status code originally returned by the triggering request. */
	@Getter
	@Setter
	private Throwable exception;
	
	/** 
	 * Constructor that also defines a message.
	 * 
	 * @param message a message explaining the cause of this exception
	 * 
	 */
	public TrustDeckClientLibraryException(String message) {
		super(message);
	}
	
	/** 
	 * Constructor that serves as a wrapper for other exceptions.
	 * A message and the original exception can be defined.
	 * 
	 * @param message a message explaining the cause of this exception
	 * @param exception the original exception
	 * 
	 */
	public TrustDeckClientLibraryException(String message, Throwable exception) {
		super(message);
		
		this.exception = exception;
		log.trace(message, exception);
	}
}
