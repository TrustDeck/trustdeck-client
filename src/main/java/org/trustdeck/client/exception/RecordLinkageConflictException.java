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

package org.trustdeck.client.exception;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.trustdeck.client.model.HttpStatusInfo;
import org.trustdeck.client.model.RecordLinkageCandidate;

/**
 * Indicates that entity creation found record-linkage candidates.
 *
 * @author Armin Müller
 */
public class RecordLinkageConflictException extends TrustDeckResponseException {

	/** Exception UID. */
	private static final long serialVersionUID = 1508461108133217324L;

	/** Candidates returned by record linkage. */
	private final List<RecordLinkageCandidate> candidates;

	/**
	 * Represents a conflict containing the backend candidates.
	 *
	 * @param status HTTP status
	 * @param info parsed backend status information
	 * @param rawBody bounded raw response body
	 * @param contentType response content type
	 * @param candidates record-linkage candidates
	 */
	public RecordLinkageConflictException(HttpStatusCode status, HttpStatusInfo info, String rawBody, String contentType, List<RecordLinkageCandidate> candidates) {
		super("Entity creation requires record-linkage resolution.", status, info, rawBody, contentType, null);

		this.candidates = List.copyOf(candidates);
	}

	/**
	 * Returns the candidates that caused the conflict.
	 *
	 * @return immutable candidate list
	 */
	public List<RecordLinkageCandidate> getCandidates() {
		return candidates;
	}
}
