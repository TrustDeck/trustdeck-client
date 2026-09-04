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
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.Entity;

/** Verifies status-dependent and non-object response conversions. */
class ResponseContractTest {
	private static HttpServer server;
	private static TrustDeckClient client;

	@BeforeAll static void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", ResponseContractTest::respond);
		server.start();
		client = new TrustDeckClient("http://localhost:" + server.getAddress().getPort(), () -> "token");
	}
	@AfterAll static void stop() { server.stop(0); }

	@Test void handlesPartialEmptyAndBinaryResponses() {
		assertEquals("created", client.domains().create(new Domain()).getName());
		var search = client.domains().search("partial");
		assertTrue(search.partial());
		assertTrue(search.items().isEmpty());
		assertTrue(client.domains().getHierarchy().isEmpty());
		assertTrue(client.pseudonyms("domain").validate("value"));
		var image = client.projectImages("project").get();
		assertEquals("image/png", image.getMimeType());
		assertArrayEquals(new byte[] { 1, 2, 3 }, image.getData());
	}

	@Test void convertsEntityConflictCandidates() {
		RecordLinkageConflictException exception = assertThrows(RecordLinkageConflictException.class, () -> client.entities("project", "type").create(new Entity()));
		assertEquals(409, exception.getStatusCode());
		assertFalse(exception.getCandidates().isEmpty());
	}

	private static void respond(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		int status = 200;
		String contentType = "application/json";
		byte[] body = "{}".getBytes();
		if (path.equals("/api/domains") && exchange.getRequestMethod().equals("POST")) body = "{\"name\":\"created\"}".getBytes();
		else if (path.equals("/api/domains")) { status = 206; body = "[]".getBytes(); }
		else if (path.endsWith("/hierarchy")) body = "{}".getBytes();
		else if (path.endsWith("/validation")) body = "\"true\"".getBytes();
		else if (path.endsWith("/image")) { contentType = "image/png"; body = new byte[] { 1, 2, 3 }; }
		else if (path.endsWith("/entities/type") && exchange.getRequestMethod().equals("POST")) { status = 409; body = "[{\"id\":\"candidate\"}]".getBytes(); }
		exchange.getResponseHeaders().set("Content-Type", contentType);
		exchange.sendResponseHeaders(status, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}
}
