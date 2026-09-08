/*
 * TrustDeck Client Library
 * Copyright 2026 Armin Müller
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

package org.trustdeck.client;

import lombok.Getter;

/**
 * Response metadata, converted body, and raw bytes.
 *
 * @param <T> converted body type
 * @author Armin Müller
 */
@Getter
public final class Response<T> {

	/** HTTP status code. */
	private final int status;

	/** Response content type. */
	private final String contentType;

	/** Response location header. */
	private final String location;

	/** Converted response body. */
	private final T body;

	/** Unmodified response bytes. */
	private final byte[] rawBody;

	/**
	 * Creates a response wrapper.
	 *
	 * @param status HTTP status code
	 * @param contentType response content type
	 * @param location response location header
	 * @param body converted response body
	 * @param rawBody unmodified response bytes
	 */
	Response(int status, String contentType, String location, T body, byte[] rawBody) {
		this.status = status;
		this.contentType = contentType;
		this.location = location;
		this.body = body;
		this.rawBody = rawBody;
	}
}
