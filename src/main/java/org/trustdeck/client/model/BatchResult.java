package org.trustdeck.client.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Ordered batch response data, preserving intentional null result slots.
 * @param <T> item type
 * @param items returned items
 * @param statusCode HTTP status code
 * @param partial whether the response is partial
 */
public record BatchResult<T>(List<T> items, int statusCode, boolean partial) {
	/** Validates and copies the result list. */
	public BatchResult {
		items = Collections.unmodifiableList(new ArrayList<>(items));
	}
}
