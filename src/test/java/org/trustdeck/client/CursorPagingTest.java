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

package org.trustdeck.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trustdeck.client.model.CursorPage;
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.service.Pseudonyms;

/**
 * Tests that the pseudonyms of a domain are read page by page by following the cursor.
 *
 * @author Armin Müller
 */
class CursorPagingTest {

	/** Pages returned by the test server, keyed by the cursor of the request ("" for the first page). */
	private static final Map<String, String> PAGES = Map.of(
			"", "{\"items\":[{\"psn\":\"TST-1\"},{\"psn\":\"TST-2\"}],\"nextCursor\":\"c1\"}",
			"c1", "{\"items\":[{\"psn\":\"TST-3\"},{\"psn\":\"TST-4\"}],\"nextCursor\":\"c2\"}",
			"c2", "{\"items\":[{\"psn\":\"TST-5\"}],\"nextCursor\":null}");

	private static HttpServer server;
	private static Pseudonyms pseudonyms;

	/** Raw query strings of the requests received by the test server. */
	private static final List<String> queries = new ArrayList<>();

	@BeforeAll
	static void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", CursorPagingTest::respond);
		server.start();
		pseudonyms = new TrustDeckClient("http://localhost:" + server.getAddress().getPort(), () -> "fake-token")
				.project("project").pseudonyms("domain");
	}

	@AfterAll
	static void stop() {
		server.stop(0);
	}

	@BeforeEach
	void clear() {
		queries.clear();
	}

	/** A single page carries its items and the cursor for the next page. */
	@Test
	void readsSinglePage() {
		CursorPage<Pseudonym> page = pseudonyms.getBatchPage("c2", 2);

		assertEquals(List.of("TST-5"), page.items().stream().map(Pseudonym::getPsn).toList());
		assertNull(page.nextCursor());
		assertEquals(List.of("cursor=c2&size=2"), queries);
	}

	/** The stream follows the cursors until the last page and returns all pseudonyms in order. */
	@Test
	void streamsAllPagesInOrder() {
		List<String> all = pseudonyms.streamBatch(2).map(Pseudonym::getPsn).toList();

		assertEquals(List.of("TST-1", "TST-2", "TST-3", "TST-4", "TST-5"), all);
		assertEquals(List.of("size=2", "cursor=c1&size=2", "cursor=c2&size=2"), queries);
	}

	/** Following pages are only requested when the stream reaches them. */
	@Test
	void requestsPagesLazily() {
		Iterator<Pseudonym> iterator = pseudonyms.streamBatch(2).iterator();
		iterator.next();
		iterator.next();

		assertEquals(1, queries.size(), queries.toString());
	}

	private static void respond(HttpExchange exchange) throws IOException {
		String query = exchange.getRequestURI().getRawQuery();
		queries.add(query == null ? "" : query);

		String cursor = "";
		if (query != null) {
			for (String part : query.split("&")) {
				if (part.startsWith("cursor=")) {
					cursor = part.substring("cursor=".length());
				}
			}
		}

		byte[] body = PAGES.get(cursor).getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}
}
