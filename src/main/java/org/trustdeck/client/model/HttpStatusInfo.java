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

package org.trustdeck.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Structured backend error status information.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
public class HttpStatusInfo {
	
	/** Creates an empty status model for JSON binding. */
	public HttpStatusInfo() { }
	
	/** Backend-reported HTTP status code. */
	private Integer statusCode;
	
	/** Backend-reported status message. */
	private String statusMessage;
}
