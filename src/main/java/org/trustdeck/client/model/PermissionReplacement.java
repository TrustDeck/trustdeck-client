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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * The result of updating a user's permissions for a resource.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
public class PermissionReplacement {
	
	/** Creates an empty result model for JSON binding. */
	public PermissionReplacement() { }

	/** Actions that were added (or would have been added). */
	private List<String> added;
	
	/** Actions that were removed (or would have been removed). */
	private List<String> removed;
	
	/** Actions the requester is not allowed to grant; if not empty, nothing was changed. */
	private List<String> rejected;
}
