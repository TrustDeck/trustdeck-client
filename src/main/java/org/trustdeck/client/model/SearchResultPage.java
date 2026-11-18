/*
 * Trust Deck Services
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

/**
 * One page of a search result, addressed by its page number.
 *
 * The backend counts the matches only up to its count limit, so that searches in very large
 * domains or entity types stay fast. When {@code numberOfMatchesExceedsSearchLimit} is
 * {@code true}, more items match than {@code total} says.
 *
 * @param <T> item type
 * @param items the items on this page
 * @param page the zero-based index of this page
 * @param size the requested maximum number of items per page
 * @param total the number of matching items, counted at most up to the backend's count limit
 * @param numberOfMatchesExceedsSearchLimit whether more items match than the count limit
 * @author Armin Müller
 */
public record SearchResultPage<T>(List<T> items, int page, int size, long total, boolean numberOfMatchesExceedsSearchLimit) {

	/** Copies the item list; a missing list becomes an empty one. */
	public SearchResultPage {
		items = items == null ? List.of() : List.copyOf(items);
	}
}
