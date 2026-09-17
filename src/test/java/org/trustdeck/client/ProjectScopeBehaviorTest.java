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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trustdeck.client.model.Algorithm;
import org.trustdeck.client.model.Domain;

/**
 * Verifies project-scope navigation, isolation, and request behavior.
 * This test class has package-private visibility because it is used only by the test suite.
 * 
 * @author Armin Müller
 */
class ProjectScopeBehaviorTest {

	/** Local HTTP server used to capture client requests. */
	private HttpServer server;

	/** Paths received by the local HTTP server. */
	private final List<String> paths = new ArrayList<>();

	/** Number of access-token requests made during a test. */
	private final AtomicInteger tokenCalls = new AtomicInteger();

	/** Number of HTTP requests received during a test. */
	private final AtomicInteger requestCalls = new AtomicInteger();

	/** TrustDeck client configured to communicate with the local test server. */
	private TrustDeckClient client;
	
	/** Body received with the most recent HTTP request. */
	private String lastBody;

	/**
	 * Starts the local HTTP server and creates the client used by each test.
	 * This method has package-private visibility because it is invoked by JUnit.
	 *
	 * @throws IOException if the local HTTP server cannot be created
	 */
	@BeforeEach
	void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", this::respond);
		server.start();
		client = new TrustDeckClient("http://localhost:" + server.getAddress().getPort(), () -> {
			tokenCalls.incrementAndGet();
			return "token";
		});
	}

	/**
	 * Stops the local HTTP server after each test.
	 * This method has package-private visibility because it is invoked by JUnit.
	 */
	@AfterEach
	void stop() {
		server.stop(0);
	}

	/**
	 * Verifies that scope navigation performs no requests and that independently
	 * selected project scopes retain their respective project abbreviations.
	 * This method has package-private visibility because it is invoked by JUnit.
	 */
	@Test
	void navigationDoesNotRequestAndScopesRemainIndependent() {
		ProjectScope first = client.project("first");
		ProjectScope second = client.project("second");
		first.image();
		first.domains();
		first.entityTypes();
		first.entities("type");
		first.pseudonyms("domain");
		first.permissions();
		first.domainPermissions("domain");
		first.entityTypePermissions("type");
		second.domains();

		assertEquals(0, requestCalls.get());
		assertEquals(0, tokenCalls.get());

		first.get("other");
		first.get();
		second.get();
		
		assertEquals(List.of("/api/projects/other", "/api/projects/first", "/api/projects/second"), paths);
	}

	/**
	 * Verifies that domain requests use a contextual copy, preserve caller data,
	 * and reject conflicting project abbreviations before making an HTTP request.
	 * This method has package-private visibility because it is invoked by JUnit.
	 */
	@Test
	void domainContextIsCopiedAndConflictsAreRejectedBeforeHttp() {
		Algorithm algorithm = Algorithm.builder().name("HASH").salt("caller-salt").build();
		Domain domain = Domain.builder().name("domain").algorithm(algorithm).build();
		
		client.project("PROJECT").domains().create(domain);

		assertNull(domain.getProjectAbbreviation());
		assertTrue(requestBody().contains("\"projectAbbreviation\":\"PROJECT\""));
		assertTrue(requestBody().contains("\"salt\":\"caller-salt\""));
		
		Domain matching = Domain.builder().projectAbbreviation("pRoJeCt").build();
		client.project("project").domains().update("domain", matching);
		
		assertEquals("pRoJeCt", matching.getProjectAbbreviation());
		assertTrue(requestBody().contains("\"projectAbbreviation\":\"pRoJeCt\""));
		
		int requests = requestCalls.get();
		assertThrows(IllegalArgumentException.class, () -> client.project("project").domains().create(Domain.builder().projectAbbreviation(" ").build()));
		assertThrows(IllegalArgumentException.class, () -> client.project("project").domains().create(Domain.builder().projectAbbreviation("other").build()));
		
		assertEquals(requests, requestCalls.get());
	}

	/**
	 * Returns the body received with the most recent HTTP request.
	 *
	 * @return most recently received request body
	 */
	private String requestBody() {
		return lastBody;
	}

	/**
	 * Records an incoming request and returns an empty JSON object.
	 *
	 * @param exchange HTTP exchange to process
	 * @throws IOException if the request cannot be read or the response cannot be written
	 */
	private void respond(HttpExchange exchange) throws IOException {
		requestCalls.incrementAndGet();
		paths.add(exchange.getRequestURI().getPath());
		lastBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		
		byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
		
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}
}
