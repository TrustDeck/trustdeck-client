/*
 * TrustDeck Client Library
 * Copyright 2025 Armin Müller
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
import org.trustdeck.client.model.HttpStatusInfo;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Exception to encapsulate non-desired responses of the TrustDeck.
 *
 * @author Armin Müller
 */
@Getter
public class TrustDeckResponseException extends RuntimeException {

	/** Exception UID. */
	@Getter(AccessLevel.NONE)
	private static final long serialVersionUID = 890444675247657376L;

	/** The status code originally returned by the triggering request. */
	private final HttpStatusCode responseStatusCode;

	/** Numeric status code. */
	private final int statusCode;

	/** Parsed backend status information. */
	private final HttpStatusInfo statusInfo;

	/** Bounded raw response body. */
	private final String rawBody;

	/** Response content type. */
	private final String contentType;

	/** Response location. */
	private final String location;

	/**
	 * Constructor that also defines the triggering status code and a message.
	 *
	 * @param message a message explaining the cause of this exception
	 * @param responseStatusCode the status code of the response of the exception-causing request
	 */
	public TrustDeckResponseException(String message, HttpStatusCode responseStatusCode) {
		this(message, responseStatusCode, null, null, null, null);
	}

	/**
	 * Creates an exception with complete response diagnostics.
	 *
	 * @param message failure message
	 * @param responseStatusCode HTTP status
	 * @param statusInfo parsed status information
	 * @param rawBody bounded raw body
	 * @param contentType response content type
	 * @param location response location
	 */
	public TrustDeckResponseException(String message, HttpStatusCode responseStatusCode, HttpStatusInfo statusInfo, String rawBody, String contentType, String location) {
		super(message);

		this.responseStatusCode = responseStatusCode;
		this.statusCode = responseStatusCode.value();
		this.statusInfo = statusInfo;
		this.rawBody = rawBody;
		this.contentType = contentType;
		this.location = location;
	}
}
