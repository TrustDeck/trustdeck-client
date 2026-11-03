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

import org.springframework.http.HttpStatusCode;
import org.trustdeck.client.model.HttpStatusInfo;
import org.trustdeck.client.model.PermissionReplacement;

/**
 * Thrown when a permission update contains actions the requester is not allowed
 * to grant. The backend changes nothing in this case.
 *
 * @author Armin Müller
 */
public class PermissionsRejectedException extends TrustDeckResponseException {

	/** Exception UID. */
	private static final long serialVersionUID = -4217093381960617257L;

	/** The backend's evaluation of the requested update. */
	private final PermissionReplacement replacement;

	/**
	 * Represents a rejected permission update.
	 *
	 * @param status HTTP status
	 * @param info parsed backend status information
	 * @param rawBody bounded raw response body
	 * @param contentType response content type
	 * @param replacement the added, removed, and rejected actions
	 */
	public PermissionsRejectedException(HttpStatusCode status, HttpStatusInfo info, String rawBody, String contentType, PermissionReplacement replacement) {
		super("The permission update contains actions the requester is not allowed to grant.", status, info, rawBody, contentType, null);

		this.replacement = replacement;
	}

	/**
	 * Returns the added, removed, and rejected actions of the update.
	 *
	 * @return the backend's evaluation of the update
	 */
	public PermissionReplacement getReplacement() {
		return replacement;
	}
}
