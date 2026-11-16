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
 * One page of a list that is read page by page with a cursor.
 *
 * The cursor is opaque: pass {@code nextCursor} back unchanged to get the following page.
 *
 * @param <T> item type
 * @param items the items on this page
 * @param nextCursor the cursor for the next page, or {@code null} on the last page
 * @author Armin Müller
 */
public record CursorPage<T>(List<T> items, String nextCursor) {

	/** Copies the item list; a missing list becomes an empty one. */
	public CursorPage {
		items = items == null ? List.of() : List.copyOf(items);
	}
}
