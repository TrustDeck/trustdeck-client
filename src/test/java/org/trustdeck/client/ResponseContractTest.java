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

package org.trustdeck.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.trustdeck.client.exception.RecordLinkageConflictException;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.Entity;
import org.trustdeck.client.model.ProjectImage;
import org.trustdeck.client.model.ProjectDomain;
import org.trustdeck.client.model.SearchResult;

/**
 * Verifies status-dependent and non-object response conversions.
 *
 * @author Armin Müller
 */
class ResponseContractTest {

	/** HTTP server used for testing the responses. */
	private static HttpServer server;

	/** TrustDeck instance to perform requests against. */
	private static TrustDeckClient client;

	/**
	 * Sets up the environment before each test.
	 */
	@BeforeAll
	static void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", ResponseContractTest::respond);
		server.start();
		client = new TrustDeckClient("http://localhost:" + server.getAddress().getPort(), () -> "token");
	}

	/**
	 * Stops the test server after each test.
	 */
	@AfterAll
	static void stop() {
		server.stop(0);
	}

	/**
	 * Verifies handling of partial, empty, validation, and binary responses.
	 */
	@Test
	void handlesPartialEmptyAndBinaryResponses() {
		ProjectScope projectScope = client.project("project");
		assertEquals("created", projectScope.domains().create(new Domain()).getName());
		
		SearchResult<Domain> search = client.domains().search("partial");
		assertTrue(search.partial());
		assertTrue(search.items().isEmpty());
		
		assertTrue(client.domains().getHierarchy().isEmpty());
		assertTrue(projectScope.pseudonyms("domain").validate("value"));
		
		ProjectImage image = projectScope.image().get();
		assertEquals("image/png", image.getMimeType());
		assertArrayEquals(new byte[] { 1, 2, 3 }, image.getData());
	}

	/**
	 * Verifies project-domain summary binding and the unfiltered search alias.
	 */
	@Test
	void bindsProjectDomainSummariesAndSharesGlobalSearch() {
		ProjectScope projectScope = client.project("project");
		ProjectDomain summary = projectScope.domains().getAll().get(0);
		
		assertEquals("summary", summary.getName());
		assertTrue(projectScope.domains().search("query").partial());
		assertTrue(client.domains().search("query").partial());
	}

	/**
	 * Verifies that entity creation conflicts are converted to a
	 * {@link RecordLinkageConflictException} containing the returned candidates.
	 */
	@Test
	void convertsEntityConflictCandidates() {
		RecordLinkageConflictException exception = assertThrows(RecordLinkageConflictException.class,
			() -> client.project("project").entities("type").create(new Entity()));
		
		assertEquals(409, exception.getStatusCode());
		assertFalse(exception.getCandidates().isEmpty());
	}

	/**
	 * Verifies that the {@code Retry-After} response header is preserved in the
	 * resulting {@link TrustDeckResponseException}.
	 */
	@Test
	void preservesRetryAfterResponseHeader() {
		TrustDeckResponseException exception = assertThrows(TrustDeckResponseException.class,
			() -> client.project("project").domains().get("retry-after"));

		assertEquals("7", exception.getRetryAfter());
	}

	/**
	 * Provides HTTP responses required by the public endpoint contract tests.
	 *
	 * @param exchange HTTP exchange to respond to
	 * @throws IOException if writing the HTTP response fails
	 */
	private static void respond(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		int status = 200;
		String contentType = "application/json";
		byte[] body = "{}".getBytes();
		
		if (path.equals("/api/domains") && exchange.getRequestMethod().equals("POST")) {
			body = "{\"name\":\"created\"}".getBytes();
		} else if (path.equals("/api/projects/project/domains")) {
			body = "[{\"name\":\"summary\",\"prefix\":\"S\",\"projectAbbreviation\":\"project\"}]".getBytes();
		} else if (path.equals("/api/domains")) {
			status = 206;
			body = "[]".getBytes();
		} else if (path.endsWith("/hierarchy")) {
			body = "{}".getBytes();
		} else if (path.endsWith("/validation")) {
			body = "\"true\"".getBytes();
		} else if (path.endsWith("/image")) {
			contentType = "image/png";
			body = new byte[] { 1, 2, 3 };
		} else if (path.equals("/api/domains/retry-after")) {
			status = 429;
			exchange.getResponseHeaders().set("Retry-After", "7");
		} else if (path.endsWith("/entities/type") && exchange.getRequestMethod().equals("POST")) {
			status = 409;
			body = "[{\"id\":\"candidate\"}]".getBytes();
		}

		exchange.getResponseHeaders().set("Content-Type", contentType);
		exchange.sendResponseHeaders(status, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}
}
