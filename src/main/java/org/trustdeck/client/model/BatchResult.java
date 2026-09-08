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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ordered batch response data, preserving intentional null result slots.
 *
 * @param <T> item type
 * @param items returned items
 * @param statusCode HTTP status code
 * @param partial whether the response is partial
 * @author Armin Müller
 */
public record BatchResult<T>(List<T> items, int statusCode, boolean partial) {

	/**
	 * Validates and copies the result list.
	 */
	public BatchResult {
		items = Collections.unmodifiableList(new ArrayList<>(items));
	}
}
