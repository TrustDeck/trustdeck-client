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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.Entity;
import org.trustdeck.client.model.EntityType;
import org.trustdeck.client.model.IdentifierItem;
import org.trustdeck.client.model.Permission;
import org.trustdeck.client.model.Project;
import org.trustdeck.client.model.ProjectImage;
import org.trustdeck.client.model.Pseudonym;
import org.trustdeck.client.model.PseudonymUpdate;
import org.trustdeck.client.model.RecordLinkageCandidate.RecordLinkageResolutionStrategy;

/**
 * Verifies that the public {@link TrustDeckClient} API maps client operations to
 * the expected HTTP endpoint contracts.
 * The tests validate HTTP methods, paths, query parameters, request bodies,
 * authentication headers, content types, and basic response handling.
 *
 * @author Armin Müller
 */
class PublicEndpointContractTest {
	
	/** Local HTTP server used to capture requests issued by the client under test. */
	private static HttpServer server;
	
	/** TrustDeck client instance configured to communicate with the local test server. */
	private static TrustDeckClient client;
	
	/** Requests captured by the local test server during the current test case. */
	private static final List<Request> requests = new ArrayList<>();
	
	/** Endpoint contract currently being executed by the dynamic test. */
	private static Case current;

	/**
	 * Starts the local HTTP server used to capture requests issued by the client.
	 *
	 * @throws IOException if the HTTP server cannot be created or started
	 */
	@BeforeAll
	static void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", PublicEndpointContractTest::respond);
		server.start();
		client = new TrustDeckClient("http://localhost:" + server.getAddress().getPort(), () -> "fake-token");
	}

	/**
	 * Stops the local HTTP server after all contract tests have completed.
	 */
	@AfterAll
	static void stop() {
		server.stop(0);
	}

	/**
	 * Creates dynamic tests covering all public client endpoint contracts.
	 *
	 * @return stream containing one dynamic test for each endpoint contract
	 */
	@TestFactory
	Stream<DynamicTest>
	endpointContracts() {
		IdentifierItem identifier = IdentifierItem.builder().identifier("id value").idType("type").build();
		Pseudonym pseudonym = Pseudonym.builder().identifierItem(identifier).build();
		PseudonymUpdate update = new PseudonymUpdate(); update.setOldIdentifierItem(identifier);
		Project project = new Project(); project.setName("project"); project.setAbbreviation("project");
		Entity entity = new Entity(); entity.setData(new ObjectMapper().valueToTree(Map.of("a", "b")));
		EntityType type = new EntityType(); type.setName("type");
		Permission permission = new Permission(); permission.setSubjectId("user");
		List<Permission> permissions = List.of(permission); UUID id = UUID.randomUUID();
		
		List<Case> cases = new ArrayList<>();
		ProjectScope projectScope = client.project("project");
		cases.add(c("ApiController health", "GET", "/api/health", Map.of(), false, true, () -> client.health()));
		cases.add(c("DomainController create", "POST", "/api/domains", Map.of(), true, true, () -> projectScope.domains().create(new Domain())));
		cases.add(c("DomainController createComplete", "POST", "/api/domains/complete", Map.of(), true, true, () -> projectScope.domains().createComplete(new Domain())));
		cases.add(c("DomainController delete", "DELETE", "/api/domains", Map.of("name", "domain", "recursive", "true"), false, true, () -> projectScope.domains().delete("domain", true)));
		cases.add(c("DomainController attribute", "GET", "/api/domains/domain/name", Map.of(), false, true, () -> projectScope.domains().getAttribute("domain", "name")));
		cases.add(c("DomainController get", "GET", "/api/domains/domain", Map.of(), false, true, () -> projectScope.domains().get("domain")));
		cases.add(c("DomainController subtree", "GET", "/api/domains/domain/subtree", Map.of(), false, true, () -> projectScope.domains().getSubtree("domain")));
		cases.add(c("DomainController hierarchy", "GET", "/api/domains/hierarchy", Map.of(), false, true, () -> client.domains().getHierarchy()));
		cases.add(c("DomainController updateComplete", "PUT", "/api/domains/complete", Map.of("name", "domain", "recursive", "true"), true, true, () -> projectScope.domains().updateComplete("domain", new Domain(), true)));
		cases.add(c("DomainController update", "PUT", "/api/domains", Map.of("name", "domain"), true, true, () -> projectScope.domains().update("domain", new Domain())));
		cases.add(c("DomainController salt", "PUT", "/api/domains/domain/salt", Map.of("salt", "12345678", "allowEmpty", "false"), false, true, () -> projectScope.domains().updateSalt("domain", "12345678", false)));
		cases.add(c("DomainController search", "GET", "/api/domains", Map.of("query", "query"), false, true, () -> client.domains().search("query")));
		cases.add(c("PseudonymController createBatch", "POST", "/api/domains/domain/pseudonyms/batch", Map.of("omitPrefix", "false"), true, true, () -> projectScope.pseudonyms("domain").createBatch(List.of(pseudonym), false)));
		cases.add(c("PseudonymController create", "POST", "/api/domains/domain/pseudonyms", Map.of("omitPrefix", "false"), true, true, () -> projectScope.pseudonyms("domain").create(pseudonym)));
		cases.add(c("PseudonymController deleteBatch", "DELETE", "/api/domains/domain/pseudonyms/batch", Map.of(), false, true, () -> projectScope.pseudonyms("domain").deleteBatch()));
		cases.add(c("PseudonymController deleteIdentifier", "DELETE", "/api/domains/domain/pseudonyms", Map.of("id", "id value", "idType", "type"), false, true, () -> projectScope.pseudonyms("domain").delete(identifier)));
		cases.add(c("PseudonymController linked", "GET", "/api/domains/linked-pseudonyms", Map.of("sourceDomain", "domain", "targetDomain", "target"), false, true, () -> projectScope.pseudonyms("domain").getLinkedPseudonyms("target", null, null, null)));
		cases.add(c("PseudonymController getBatch", "GET", "/api/domains/domain/pseudonyms/batch", Map.of(), false, true, () -> projectScope.pseudonyms("domain").getBatch()));
		cases.add(c("PseudonymController getIdentifier", "GET", "/api/domains/domain/pseudonyms", Map.of("id", "id value", "idType", "type"), false, true, () -> projectScope.pseudonyms("domain").get(identifier)));
		cases.add(c("PseudonymController getPsn", "GET", "/api/domains/domain/pseudonyms", Map.of("psn", "psn"), false, true, () -> projectScope.pseudonyms("domain").get("psn")));
		cases.add(c("PseudonymController updateBatch", "PUT", "/api/domains/domain/pseudonyms/batch", Map.of(), true, true, () -> projectScope.pseudonyms("domain").updateBatch(List.of(update))));
		cases.add(c("PseudonymController updateComplete", "PUT", "/api/domains/domain/pseudonyms/complete", Map.of("regeneratePseudonym", "true"), true, true, () -> projectScope.pseudonyms("domain").updateComplete(update, true)));
		cases.add(c("PseudonymController update", "PUT", "/api/domains/domain/pseudonyms", Map.of(), true, true, () -> projectScope.pseudonyms("domain").update(update)));
		cases.add(c("PseudonymController validate", "GET", "/api/domains/domain/pseudonyms/validation", Map.of("psn", "psn"), false, true, () -> projectScope.pseudonyms("domain").validate("psn")));
		cases.add(c("PseudonymController search", "GET", "/api/domains/domain/pseudonyms", Map.of("query", "query"), false, true, () -> projectScope.pseudonyms("domain").search("query")));
		cases.add(c("ProjectController create", "POST", "/api/projects", Map.of(), true, true, () -> client.projects().create(project)));
		cases.add(c("ProjectController list", "GET", "/api/projects", Map.of(), false, true, () -> client.projects().getAll()));
		cases.add(c("ProjectController get", "GET", "/api/projects/project", Map.of(), false, true, () -> client.projects().get("project")));
		cases.add(c("ProjectController domains", "GET", "/api/projects/project/domains", Map.of(), false, true, () -> projectScope.domains().getAll()));
		cases.add(c("ProjectController statistics", "GET", "/api/projects/project/statistics", Map.of(), false, false, () -> { try { return projectScope.getStatistics(); } catch (TrustDeckResponseException e) { assertEquals(501, e.getStatusCode()); return null; } }));
		cases.add(c("ProjectController update", "PUT", "/api/projects/project", Map.of(), true, true, () -> projectScope.update(project)));
		cases.add(c("ProjectController delete", "DELETE", "/api/projects/project", Map.of(), false, true, () -> projectScope.delete()));
		cases.add(c("ProjectImageController create", "POST", "/api/projects/project/image", Map.of(), true, true, () -> projectScope.image().create(new ProjectImage(new byte[] {1, 2, 3}, "image/png", "logo.png"))));
		cases.add(c("ProjectImageController get", "GET", "/api/projects/project/image", Map.of(), false, true, () -> projectScope.image().get()));
		cases.add(c("ProjectImageController update", "PUT", "/api/projects/project/image", Map.of(), true, true, () -> projectScope.image().update(new ProjectImage(new byte[] {1, 2, 3}, "image/png", "logo.png"))));
		cases.add(c("ProjectImageController delete", "DELETE", "/api/projects/project/image", Map.of(), false, true, () -> projectScope.image().delete()));
		cases.add(c("EntityTypeController baseCreate", "POST", "/api/entities/base-types", Map.of(), true, true, () -> client.baseEntityTypes().create(type)));
		cases.add(c("EntityTypeController baseGet", "GET", "/api/entities/base-types/type", Map.of(), false, true, () -> client.baseEntityTypes().get("type")));
		cases.add(c("EntityTypeController baseSearch", "GET", "/api/entities/base-types", Map.of("query", "query"), false, true, () -> client.baseEntityTypes().search("query")));
		cases.add(c("EntityTypeController create", "POST", "/api/projects/project/entities/config", Map.of(), true, true, () -> projectScope.entityTypes().create(type)));
		cases.add(c("EntityTypeController get", "GET", "/api/projects/project/entities/config/type", Map.of(), false, true, () -> projectScope.entityTypes().get("type")));
		cases.add(c("EntityTypeController update", "PUT", "/api/projects/project/entities/config/type", Map.of(), true, true, () -> projectScope.entityTypes().update("type", type)));
		cases.add(c("EntityTypeController delete", "DELETE", "/api/projects/project/entities/config/type", Map.of(), false, true, () -> projectScope.entityTypes().delete("type")));
		cases.add(c("EntityTypeController search", "GET", "/api/projects/project/entities", Map.of("query", "query"), false, true, () -> projectScope.entityTypes().search("query")));
		cases.add(c("EntityController create", "POST", "/api/projects/project/entities/type", Map.of("recordLinkageResolution", "CREATE_ORIGINAL"), true, true, () -> projectScope.entities("type").create(entity, RecordLinkageResolutionStrategy.CREATE_ORIGINAL)));
		cases.add(c("EntityController get", "GET", "/api/projects/project/entities/type/" + id, Map.of(), false, true, () -> projectScope.entities("type").get(id)));
		cases.add(c("EntityController update", "PUT", "/api/projects/project/entities/type/" + id, Map.of(), true, true, () -> projectScope.entities("type").update(id, entity)));
		cases.add(c("EntityController delete", "DELETE", "/api/projects/project/entities/type/" + id, Map.of(), false, true, () -> projectScope.entities("type").delete(id)));
		cases.add(c("EntityController search", "GET", "/api/projects/project/entities/type", Map.of("query", "query"), false, true, () -> projectScope.entities("type").search("query")));
		cases.add(c("EntityController pseudonyms", "GET", "/api/projects/project/entities/type/" + id + "/pseudonyms", Map.of(), false, true, () -> projectScope.entities("type").getPseudonyms(id)));
		cases.add(c("EntityController recordLinkage", "POST", "/api/projects/project/entities/type/record-linkage", Map.of(), true, true, () -> projectScope.entities("type").findRecordLinkageCandidates(entity)));
		cases.add(c("PermissionController userSearch", "GET", "/api/permissions/users", Map.of("query", "query"), false, true, () -> client.permissions().searchUsers("query")));
		cases.add(c("PermissionController domainCreate", "POST", "/api/permissions/domains/domain", Map.of("userId", "user"), true, true, () -> projectScope.domainPermissions("domain").create("user", permissions)));
		cases.add(c("PermissionController projectCreate", "POST", "/api/permissions/projects/project", Map.of("userId", "user"), true, true, () -> projectScope.permissions().create("user", permissions)));
		cases.add(c("PermissionController globalCreate", "POST", "/api/permissions/global", Map.of("userId", "user"), true, true, () -> client.permissions().createGlobal("user", permissions)));
		cases.add(c("PermissionController typeCreate", "POST", "/api/permissions/projects/project/entity-types/type", Map.of("userId", "user"), true, true, () -> projectScope.entityTypePermissions("type").create("user", permissions)));
		cases.add(c("PermissionController domainGet", "GET", "/api/permissions/domains/domain", Map.of("userId", "user"), false, true, () -> projectScope.domainPermissions("domain").get("user")));
		cases.add(c("PermissionController projectGet", "GET", "/api/permissions/projects/project", Map.of("userId", "user"), false, true, () -> projectScope.permissions().get("user")));
		cases.add(c("PermissionController globalGet", "GET", "/api/permissions/global", Map.of("userId", "user"), false, true, () -> client.permissions().getGlobal("user")));
		cases.add(c("PermissionController typeGet", "GET", "/api/permissions/projects/project/entity-types/type", Map.of("userId", "user"), false, true, () -> projectScope.entityTypePermissions("type").get("user")));
		cases.add(c("PermissionController available", "GET", "/api/permissions", Map.of(), false, true, () -> client.permissions().getAvailable()));
		cases.add(c("PermissionController domainUpdate", "PUT", "/api/permissions/domains/domain", Map.of("userId", "user"), true, true, () -> projectScope.domainPermissions("domain").update("user", permissions)));
		cases.add(c("PermissionController projectUpdate", "PUT", "/api/permissions/projects/project", Map.of("userId", "user"), true, true, () -> projectScope.permissions().update("user", permissions)));
		cases.add(c("PermissionController globalUpdate", "PUT", "/api/permissions/global", Map.of("userId", "user"), true, true, () -> client.permissions().updateGlobal("user", permissions)));
		cases.add(c("PermissionController typeUpdate", "PUT", "/api/permissions/projects/project/entity-types/type", Map.of("userId", "user"), true, true, () -> projectScope.entityTypePermissions("type").update("user", permissions)));
		cases.add(c("PermissionController domainDelete", "DELETE", "/api/permissions/domains/domain", Map.of("userId", "user"), true, true, () -> projectScope.domainPermissions("domain").delete("user", permissions)));
		cases.add(c("PermissionController projectDelete", "DELETE", "/api/permissions/projects/project", Map.of("userId", "user"), true, true, () -> projectScope.permissions().delete("user", permissions)));
		cases.add(c("PermissionController globalDelete", "DELETE", "/api/permissions/global", Map.of("userId", "user"), true, true, () -> client.permissions().deleteGlobal("user", permissions)));
		cases.add(c("PermissionController typeDelete", "DELETE", "/api/permissions/projects/project/entity-types/type", Map.of("userId", "user"), true, true, () -> projectScope.entityTypePermissions("type").delete("user", permissions)));
		assertEquals(69, cases.size());
		
		return cases.stream().map(spec -> DynamicTest.dynamicTest(spec.name, () -> run(spec)));
	}

	/**
	 * Creates an endpoint contract test case.
	 *
	 * @param name descriptive name of the test case
	 * @param method expected HTTP method
	 * @param path expected request path
	 * @param query expected query parameters
	 * @param body whether a request body is expected
	 * @param result whether a non-null result is expected
	 * @param action client operation exercising the endpoint
	 * @return configured endpoint contract test case
	 */
	private static Case c(String name, String method, String path, Map<String, String> query, boolean body, boolean result, Supplier<?> action) {
		return new Case(name, method, path, query, body, result, action);
	}
	
	/**
	 * Executes an endpoint contract test case and verifies the captured request
	 * against its expected HTTP contract.
	 *
	 * @param spec endpoint contract to execute and verify
	 */
	private static void run(Case spec) {
		requests.clear();
		current = spec;
		Object result = spec.action.get();

		assertEquals(1, requests.size());
		
		Request request = requests.get(0);
		assertEquals(spec.method, request.method);
		assertEquals(spec.path, request.path);
		assertEquals(spec.query, query(request.query));
		assertEquals("application/json", request.accept);
		assertEquals("GET".equals(spec.method) && spec.path.equals("/api/health") ? null : "Bearer fake-token", request.authorization);

		if (spec.body) {
			assertTrue(request.contentType.startsWith(spec.path.endsWith("/image") ? "multipart/form-data" : "application/json"));
			if (spec.path.endsWith("/image")) {
				String multipart = new String(request.body, StandardCharsets.ISO_8859_1);
				assertTrue(multipart.contains("name=\"image\""));
				assertTrue(multipart.contains("filename=\"logo.png\""));
				assertTrue(multipart.contains("Content-Type: image/png"));
				assertTrue(containsBytes(request.body, new byte[] { 1, 2, 3 }));
			}
		} else {
			assertFalse(request.body.length > 0);
		}
		
		if (spec.result) {
			assertNotNull(result);
		}
	}

	/**
	 * Parses a raw URL query string into decoded parameter names and values.
	 *
	 * @param raw raw query string, or {@code null} if no query is present
	 * @return decoded query parameters
	 */
	private static Map<String, String> query(String raw) {
		Map<String, String> result = new LinkedHashMap<>();
		
		if (raw == null) {
			return result;
		}
		
		for (String item : raw.split("&")) {
			String[] pair = item.split("=", 2);
			result.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
					URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
		}
		
		return result;
	}

	/**
	 * Checks whether a byte sequence occurs within another byte array.
	 *
	 * @param actual byte array to search
	 * @param expected byte sequence to locate
	 * @return {@code true} if the expected sequence occurs in the actual data, otherwise {@code false}
	 */
	private static boolean containsBytes(byte[] actual, byte[] expected) {
		for (int i = 0; i <= actual.length - expected.length; i++) {
			if (Arrays.equals(expected, Arrays.copyOfRange(actual, i, i + expected.length))) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Captures an incoming request and returns a response appropriate for the
	 * currently executed endpoint contract.
	 *
	 * @param exchange HTTP exchange to process
	 * @throws IOException if reading the request or writing the response fails
	 */
	private static void respond(HttpExchange exchange) throws IOException {
		byte[] requestBody = exchange.getRequestBody().readAllBytes();
		
		requests.add(new Request(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
				exchange.getRequestURI().getRawQuery(), exchange.getRequestHeaders().getFirst("Authorization"),
				exchange.getRequestHeaders().getFirst("Accept"), exchange.getRequestHeaders().getFirst("Content-Type"),
				requestBody));
		
		int status = current.path.endsWith("/statistics") ? 501
				: current.method.equals("DELETE") ? 204
						: current.path.endsWith("record-linkage") ? 200 : current.method.equals("POST") ? 201 : 200;
		
		byte[] response = current.path.equals("/api/health")
				? "{\"status\":\"UP\",\"service\":\"test\",\"timestamp\":\"2026-01-01T00:00:00Z\"}".getBytes(StandardCharsets.UTF_8)
				: current.path.endsWith("/name") 
						? "\"name\"".getBytes(StandardCharsets.UTF_8)
						: current.path.endsWith("/validation") 
								? "true".getBytes(StandardCharsets.UTF_8)
								: current.path.endsWith("/image") && current.method.equals("GET")
										? new byte[] { 1, 2, 3 }
										: "{}".getBytes(StandardCharsets.UTF_8);
		
		if (current.path.endsWith("/image") && current.method.equals("GET")) {
			exchange.getResponseHeaders().set("Content-Type", "image/png");
		} else {
			exchange.getResponseHeaders().set("Content-Type", "application/json");
		}
		
		exchange.sendResponseHeaders(status, status == 204 ? -1 : response.length);
		
		if (status != 204) {
			exchange.getResponseBody().write(response);
		}
		
		exchange.close();
	}

	/**
	 * Defines the expected HTTP contract and client operation for a dynamic test.
	 *
	 * @param name descriptive name of the test
	 * @param method expected HTTP method
	 * @param path expected request path
	 * @param query expected query parameters
	 * @param body whether a request body is expected
	 * @param result whether a non-null result is expected
	 * @param action client operation exercising the endpoint
	 */
	private record Case(String name, String method, String path, Map<String, String> query, boolean body, boolean result, Supplier<?> action) { }

	/**
	 * Represents an HTTP request captured by the local test server.
	 *
	 * @param method HTTP method
	 * @param path request path
	 * @param query raw query string
	 * @param authorization authorization header value
	 * @param accept accept header value
	 * @param contentType content type header value
	 * @param body request body
	 */
	private record Request(String method, String path, String query, String authorization, String accept, String contentType, byte[] body) { }
}
