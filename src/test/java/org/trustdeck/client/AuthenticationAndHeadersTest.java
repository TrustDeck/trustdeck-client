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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.config.TrustDeckClientConfig.GrantType;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Verifies the client credentials grant and the additional on-behalf-of and purpose headers.
 *
 * @author Armin Müller
 */
class AuthenticationAndHeadersTest {

	/** Local server acting as both Keycloak and TrustDeck. */
	private HttpServer server;

	/** Form parameters of the token requests received by the fake Keycloak. */
	private final List<Map<String, String>> tokenRequests = new CopyOnWriteArrayList<>();

	/** Headers of the API requests received by the fake TrustDeck. */
	private final List<Map<String, String>> apiRequests = new CopyOnWriteArrayList<>();

	/** Base URL of the local server. */
	private String baseUrl;

	/**
	 * Starts the local server with a token endpoint and a pseudonym endpoint.
	 *
	 * @throws IOException if the server cannot be started
	 */
	@BeforeEach
	void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/realms/test/protocol/openid-connect/token", this::token);
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
	 * The builder keeps the password grant as default so existing configurations are unchanged.
	 */
	@Test
	void passwordGrantIsTheDefault() {
		TrustDeckClientConfig config = TrustDeckClientConfig.builder().serviceUrl(baseUrl).build();

		assertEquals(GrantType.PASSWORD, config.getGrantType());
		assertFalse(config.usesClientCredentials());
		assertEquals(GrantType.PASSWORD, new TrustDeckClientConfig().getGrantType());
		assertEquals(GrantType.PASSWORD, new TrustDeckClientConfig(baseUrl, baseUrl, "test", "client", "secret", "user", "pw").getGrantType());
	}

	/**
	 * The password grant still requires user name and password, the client credentials grant does not.
	 */
	@Test
	void userCredentialsAreOnlyRequiredForThePasswordGrant() {
		TrustDeckClientConfig password = TrustDeckClientConfig.builder().serviceUrl(baseUrl).keycloakUrl(baseUrl)
				.realm("test").clientId("client").clientSecret("secret").build();
		IllegalArgumentException missing = assertThrows(IllegalArgumentException.class, () -> new TrustDeckClient(password));
		assertTrue(missing.getMessage().contains("userName"));

		TrustDeckClientConfig clientCredentials = TrustDeckClientConfig.clientCredentials(baseUrl, baseUrl, "test", "client", "secret");
		assertTrue(clientCredentials.usesClientCredentials());
		new TrustDeckClient(clientCredentials);

		TrustDeckClientConfig missingSecret = TrustDeckClientConfig.builder().serviceUrl(baseUrl).keycloakUrl(baseUrl)
				.realm("test").clientId("client").grantType(GrantType.CLIENT_CREDENTIALS).build();
		assertThrows(IllegalArgumentException.class, () -> new TrustDeckClient(missingSecret));
	}

	/**
	 * With the client credentials grant, the token is requested without user credentials and is used as bearer token.
	 */
	@Test
	void clientCredentialsGrantObtainsAndUsesAServiceAccountToken() {
		TrustDeckClient client = new TrustDeckClient(TrustDeckClientConfig.clientCredentials(baseUrl, baseUrl, "test", "client", "secret"));

		client.project("project").pseudonyms("domain").get("psn");

		assertEquals(1, tokenRequests.size());
		Map<String, String> form = tokenRequests.get(0);
		assertEquals("client_credentials", form.get("grant_type"));
		assertFalse(form.containsKey("username"));
		assertFalse(form.containsKey("password"));
		assertEquals("Bearer service-account-token", apiRequests.get(0).get("authorization"));
	}

	/**
	 * The password grant keeps sending user name and password.
	 */
	@Test
	void passwordGrantSendsUserCredentials() {
		TrustDeckClient client = new TrustDeckClient(new TrustDeckClientConfig(baseUrl, baseUrl, "test", "client", "secret", "user", "pw"));

		client.project("project").pseudonyms("domain").get("psn");

		Map<String, String> form = tokenRequests.get(0);
		assertEquals("password", form.get("grant_type"));
		assertEquals("user", form.get("username"));
		assertEquals("pw", form.get("password"));
	}

	/**
	 * A client view sends the on-behalf-of and purpose headers; the original client does not.
	 */
	@Test
	void onBehalfOfViewSendsAuditHeaders() {
		TrustDeckClient client = new TrustDeckClient(baseUrl, () -> "token");

		client.onBehalfOf("jdoe", "Re-identification\r\nInjected: header").project("project").pseudonyms("domain").get("psn");
		client.project("project").pseudonyms("domain").get("psn");
		client.onBehalfOf("jdoe", null).project("project").pseudonyms("domain").get("psn");

		assertEquals("jdoe", apiRequests.get(0).get("x-on-behalf-of"));
		assertEquals("Re-identification  Injected: header", apiRequests.get(0).get("x-purpose"));
		assertNull(apiRequests.get(0).get("injected"));
		assertNull(apiRequests.get(1).get("x-on-behalf-of"));
		assertNull(apiRequests.get(1).get("x-purpose"));
		assertEquals("jdoe", apiRequests.get(2).get("x-on-behalf-of"));
		assertNull(apiRequests.get(2).get("x-purpose"));
		assertEquals("Bearer token", apiRequests.get(0).get("authorization"));
	}

	/**
	 * Header values are trimmed, stripped of control characters, limited in length, and dropped when blank.
	 */
	@Test
	void headerValuesAreSanitized() {
		assertNull(TrustDeckHttpClient.sanitizeHeaderValue(null));
		assertNull(TrustDeckHttpClient.sanitizeHeaderValue(" \t\r\n "));
		assertEquals("a b", TrustDeckHttpClient.sanitizeHeaderValue(" a\nb "));
		assertEquals(1024, TrustDeckHttpClient.sanitizeHeaderValue("x".repeat(2000)).length());
	}

	/**
	 * Answers token requests like Keycloak and records their form parameters.
	 *
	 * @param exchange the HTTP exchange
	 * @throws IOException if reading or writing fails
	 */
	private void token(HttpExchange exchange) throws IOException {
		tokenRequests.add(form(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));

		byte[] body = "{\"access_token\":\"service-account-token\",\"expires_in\":300,\"token_type\":\"Bearer\"}"
				.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}

	/**
	 * Answers API requests with an empty JSON object and records their headers.
	 *
	 * @param exchange the HTTP exchange
	 * @throws IOException if writing fails
	 */
	private void api(HttpExchange exchange) throws IOException {
		Map<String, String> headers = new LinkedHashMap<>();
		exchange.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(), values.get(0)));
		apiRequests.add(headers);

		byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}

	/**
	 * Parses an URL-encoded form body.
	 *
	 * @param raw the raw body
	 * @return the decoded parameters
	 */
	private static Map<String, String> form(String raw) {
		Map<String, String> result = new LinkedHashMap<>();
		for (String item : raw.split("&")) {
			if (item.isEmpty()) {
				continue;
			}

			String[] pair = item.split("=", 2);
			result.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
					pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "");
		}

		return result;
	}
}
