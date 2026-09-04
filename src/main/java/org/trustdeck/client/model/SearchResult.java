package org.trustdeck.client.model;

import java.util.List;

/** A search result, including whether the backend truncated it.
 * @param <T> item type
 * @param items returned items
 * @param partial whether the result was truncated
 */
public record SearchResult<T>(List<T> items, boolean partial) {
	/** Copies the result list. */
	public SearchResult {
		items = List.copyOf(items);
	}
}
