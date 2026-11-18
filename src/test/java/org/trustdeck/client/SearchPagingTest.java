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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.Entity;
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.model.SearchResultPage;

/**
 * Tests that pages of pseudonym and entity searches are read with their paging information.
 *
 * @author Armin Müller
 */
class SearchPagingTest {

	private static HttpServer server;
	private static TrustDeckClient client;

	@BeforeAll
	static void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", SearchPagingTest::respond);
		server.start();
		client = new TrustDeckClient("http://localhost:" + server.getAddress().getPort(), () -> "fake-token");
	}

	@AfterAll
	static void stop() {
		server.stop(0);
	}

	/** A pseudonym page carries its items, its position, and the capped total. */
	@Test
	void readsPseudonymPage() {
		SearchResultPage<Pseudonym> page = client.project("project").pseudonyms("domain").searchPage("TST", 1, 2);

		assertEquals(2, page.items().size());
		assertEquals("TST-3", page.items().get(0).getPsn());
		assertEquals(1, page.page());
		assertEquals(2, page.size());
		assertEquals(10_000, page.total());
		assertTrue(page.numberOfMatchesExceedsSearchLimit());
	}

	/** An entity page is read the same way; an empty page has no items. */
	@Test
	void readsEntityPage() {
		SearchResultPage<Entity> page = client.project("project").entities("type").searchPage("nobody", 0, null);

		assertTrue(page.items().isEmpty());
		assertEquals(0, page.total());
		assertFalse(page.numberOfMatchesExceedsSearchLimit());
	}

	/** Sorting by an attribute without a sort index is refused by the backend with 422. */
	@Test
	void reportsUnindexedSort() {
		TrustDeckResponseException exception = assertThrows(TrustDeckResponseException.class,
				() -> client.project("project").entities("type").searchPage("*", 0, 20, null, "attribute:city", "asc"));
		assertEquals(422, exception.getStatusCode());
	}

	private static void respond(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		String query = exchange.getRequestURI().getRawQuery();
		int status = 200;
		String body;
		if (query != null && query.contains("sort=attribute")) {
			status = 422;
			body = "";
		} else if (path.endsWith("/pseudonyms")) {
			body = "{\"items\":[{\"psn\":\"TST-3\"},{\"psn\":\"TST-4\"}],\"page\":1,\"size\":2,\"total\":10000,"
					+ "\"numberOfMatchesExceedsSearchLimit\":true}";
		} else {
			body = "{\"items\":[],\"page\":0,\"size\":20,\"total\":0,\"numberOfMatchesExceedsSearchLimit\":false}";
		}

		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
		if (bytes.length > 0) {
			exchange.getResponseBody().write(bytes);
		}
		exchange.close();
	}
}
