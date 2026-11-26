/*
 * TrustDeck Client Library
 * Copyright 2025 Armin Müller
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trustdeck.client.model.Entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Verifies that JSON request bodies are written by the client's own Jackson 2 mapper. Applications on
 * Spring Framework 7 (Spring Boot 4) have Jackson 3 on the classpath, which RestClient would otherwise
 * use and which cannot write Jackson 2 types such as {@link JsonNode}.
 *
 * @author Armin Müller
 */
class RequestBodySerializationTest {

	/** Local server acting as TrustDeck. */
	private HttpServer server;

	/** Bodies and content types of the requests received by the fake TrustDeck. */
	private final List<String[]> requests = new CopyOnWriteArrayList<>();

	/** Base URL of the local server. */
	private String baseUrl;

	/**
	 * Starts the local server.
	 *
	 * @throws IOException if the server cannot be started
	 */
	@BeforeEach
	void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/api/", this::api);
		server.start();
		baseUrl = "http://localhost:" + server.getAddress().getPort();
	}

	/**
	 * Stops the local server.
	 */
	@AfterEach
	void stop() {
		server.stop(0);
	}

	/**
	 * Entity data given as a Jackson 2 {@link JsonNode} arrives as a plain JSON object.
	 *
	 * @throws Exception if the received JSON cannot be parsed
	 */
	@Test
	void jsonNodeEntityDataIsWrittenAsJson() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = mapper.readTree("{\"name\":\"Doe\",\"birth\":{\"year\":1970}}");
		TrustDeckClient client = new TrustDeckClient(baseUrl, () -> "token");

		client.project("project").entities("person").create(Entity.builder().data(data).build());

		assertEquals("application/json", requests.get(0)[1]);
		assertEquals(data, mapper.readTree(requests.get(0)[0]).get("data"));
	}

	/**
	 * A list of pseudonyms arrives as a JSON array.
	 *
	 * @throws Exception if the received JSON cannot be parsed
	 */
	@Test
	void listBodyIsWrittenAsJsonArray() throws Exception {
		TrustDeckClient client = new TrustDeckClient(baseUrl, () -> "token");

		client.project("project").pseudonyms("domain").lookup(List.of("a", "b"));

		assertEquals(new ObjectMapper().readTree("[\"a\",\"b\"]"), new ObjectMapper().readTree(requests.get(0)[0]));
	}

	/**
	 * Answers API requests and records their bodies and content types.
	 *
	 * @param exchange the HTTP exchange
	 * @throws IOException if reading or writing fails
	 */
	private void api(HttpExchange exchange) throws IOException {
		requests.add(new String[] { new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8),
				exchange.getRequestHeaders().getFirst("Content-Type") });

		byte[] body = (exchange.getRequestURI().getPath().endsWith("/lookup") ? "[]" : "{}").getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(exchange.getRequestURI().getPath().endsWith("/lookup") ? 200 : 201, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}
}
