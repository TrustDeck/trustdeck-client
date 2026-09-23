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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.trustdeck.client.exception.TrustDeckClientLibraryException;

/**
 * Verifies that configured transport timeouts are applied to the public client.
 *
 * @author Armin Müller
 */
class HttpTimeoutTest {

	/** HTTP server used to delay a response. */
	private HttpServer server;

	/**
	 * Stops the test server after each test.
	 */
	@AfterEach
	void stopServer() {
		if (server != null) {
			server.stop(0);
		}
	}

	/**
	 * Confirms that the read timeout interrupts a delayed response.
	 *
	 * @throws IOException if the test server cannot be started
	 */
	@Test
	void appliesConfiguredReadTimeout() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/api/health", this::delayResponse);
		server.start();

		TrustDeckClient client = new TrustDeckClient(
				"http://localhost:" + server.getAddress().getPort(), () -> "token",
				Duration.ofSeconds(1), Duration.ofMillis(50));

		assertThrows(TrustDeckClientLibraryException.class, client::health);
	}

	/**
	 * Delays a response longer than the configured read timeout.
	 *
	 * @param exchange HTTP exchange
	 */
	private void delayResponse(HttpExchange exchange) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
	}
}
