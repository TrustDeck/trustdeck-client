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
import org.trustdeck.client.model.RecordLinkageCandidate.RecordLinkageResolution;

/** Independently exercises every reviewed backend operation through the public API. */
class PublicEndpointContractTest {
	private static HttpServer server;
	private static TrustDeckClient client;
	private static final List<Request> requests = new ArrayList<>();
	private static Case current;

	@BeforeAll static void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/", PublicEndpointContractTest::respond);
		server.start();
		client = new TrustDeckClient("http://localhost:" + server.getAddress().getPort(), () -> "fake-token");
	}
	@AfterAll static void stop() { server.stop(0); }

	@TestFactory Stream<DynamicTest> endpointContracts() {
		IdentifierItem identifier = IdentifierItem.builder().identifier("id value").idType("type").build();
		Pseudonym pseudonym = Pseudonym.builder().identifierItem(identifier).build();
		PseudonymUpdate update = new PseudonymUpdate(); update.setOldIdentifierItem(identifier);
		Project project = new Project(); project.setName("project"); project.setAbbreviation("project");
		Entity entity = new Entity(); entity.setData(new ObjectMapper().valueToTree(Map.of("a", "b")));
		EntityType type = new EntityType(); type.setName("type");
		Permission permission = new Permission(); permission.setSubjectId("user");
		List<Permission> permissions = List.of(permission); UUID id = UUID.randomUUID();
		List<Case> cases = new ArrayList<>();
		cases.add(c("ApiController health", "GET", "/api/health", Map.of(), false, true, () -> client.health()));
		cases.add(c("DomainController create", "POST", "/api/domains", Map.of(), true, true, () -> client.domains().create(new Domain())));
		cases.add(c("DomainController createComplete", "POST", "/api/domains/complete", Map.of(), true, true, () -> client.domains().createComplete(new Domain())));
		cases.add(c("DomainController delete", "DELETE", "/api/domains", Map.of("name", "domain", "recursive", "true"), false, true, () -> client.domains().delete("domain", true)));
		cases.add(c("DomainController attribute", "GET", "/api/domains/domain/name", Map.of(), false, true, () -> client.domains().getAttribute("domain", "name")));
		cases.add(c("DomainController get", "GET", "/api/domains/domain", Map.of(), false, true, () -> client.domains().get("domain")));
		cases.add(c("DomainController subtree", "GET", "/api/domains/domain/subtree", Map.of(), false, true, () -> client.domains().getSubtree("domain")));
		cases.add(c("DomainController hierarchy", "GET", "/api/domains/hierarchy", Map.of(), false, true, () -> client.domains().getHierarchy()));
		cases.add(c("DomainController updateComplete", "PUT", "/api/domains/complete", Map.of("name", "domain", "recursive", "true"), true, true, () -> client.domains().updateComplete("domain", new Domain(), true)));
		cases.add(c("DomainController update", "PUT", "/api/domains", Map.of("name", "domain"), true, true, () -> client.domains().update("domain", new Domain())));
		cases.add(c("DomainController salt", "PUT", "/api/domains/domain/salt", Map.of("salt", "12345678", "allowEmpty", "false"), false, true, () -> client.domains().updateSalt("domain", "12345678", false)));
		cases.add(c("DomainController search", "GET", "/api/domains", Map.of("query", "query"), false, true, () -> client.domains().search("query")));
		cases.add(c("PseudonymController createBatch", "POST", "/api/domains/domain/pseudonyms/batch", Map.of("omitPrefix", "false"), true, true, () -> client.pseudonyms("domain").createBatch(List.of(pseudonym), false)));
		cases.add(c("PseudonymController create", "POST", "/api/domains/domain/pseudonyms", Map.of("omitPrefix", "false"), true, true, () -> client.pseudonyms("domain").create(pseudonym)));
		cases.add(c("PseudonymController deleteBatch", "DELETE", "/api/domains/domain/pseudonyms/batch", Map.of(), false, true, () -> client.pseudonyms("domain").deleteBatch()));
		cases.add(c("PseudonymController deleteIdentifier", "DELETE", "/api/domains/domain/pseudonyms", Map.of("id", "id value", "idType", "type"), false, true, () -> client.pseudonyms("domain").delete(identifier)));
		cases.add(c("PseudonymController linked", "GET", "/api/domains/linked-pseudonyms", Map.of("sourceDomain", "source", "targetDomain", "target"), false, true, () -> client.pseudonyms("domain").getLinkedPseudonyms("source", "target", null, null, null)));
		cases.add(c("PseudonymController getBatch", "GET", "/api/domains/domain/pseudonyms/batch", Map.of(), false, true, () -> client.pseudonyms("domain").getBatch()));
		cases.add(c("PseudonymController getIdentifier", "GET", "/api/domains/domain/pseudonyms", Map.of("id", "id value", "idType", "type"), false, true, () -> client.pseudonyms("domain").get(identifier)));
		cases.add(c("PseudonymController getPsn", "GET", "/api/domains/domain/pseudonyms", Map.of("psn", "psn"), false, true, () -> client.pseudonyms("domain").get("psn")));
		cases.add(c("PseudonymController updateBatch", "PUT", "/api/domains/domain/pseudonyms/batch", Map.of(), true, true, () -> client.pseudonyms("domain").updateBatch(List.of(update))));
		cases.add(c("PseudonymController updateComplete", "PUT", "/api/domains/domain/pseudonyms/complete", Map.of("regeneratePseudonym", "true"), true, true, () -> client.pseudonyms("domain").updateComplete(update, true)));
		cases.add(c("PseudonymController update", "PUT", "/api/domains/domain/pseudonyms", Map.of(), true, true, () -> client.pseudonyms("domain").update(update)));
		cases.add(c("PseudonymController validate", "GET", "/api/domains/domain/pseudonyms/validation", Map.of("psn", "psn"), false, true, () -> client.pseudonyms("domain").validate("psn")));
		cases.add(c("PseudonymController search", "GET", "/api/domains/domain/pseudonyms", Map.of("query", "query"), false, true, () -> client.pseudonyms("domain").search("query")));
		cases.add(c("ProjectController create", "POST", "/api/projects", Map.of(), true, true, () -> client.projects().create(project)));
		cases.add(c("ProjectController list", "GET", "/api/projects", Map.of(), false, true, () -> client.projects().getAll()));
		cases.add(c("ProjectController get", "GET", "/api/projects/project", Map.of(), false, true, () -> client.projects().get("project")));
		cases.add(c("ProjectController domains", "GET", "/api/projects/project/domains", Map.of(), false, true, () -> client.projects().getDomains("project")));
		cases.add(c("ProjectController statistics", "GET", "/api/projects/project/statistics", Map.of(), false, false, () -> { try { return client.projects().getStatistics("project"); } catch (TrustDeckResponseException e) { assertEquals(501, e.getStatusCode()); return null; } }));
		cases.add(c("ProjectController update", "PUT", "/api/projects/project", Map.of(), true, true, () -> client.projects().update("project", project)));
		cases.add(c("ProjectController delete", "DELETE", "/api/projects/project", Map.of(), false, true, () -> client.projects().deleteNow("project")));
		cases.add(c("ProjectImageController create", "POST", "/api/projects/project/image", Map.of(), true, true, () -> client.projectImages("project").create(new ProjectImage(new byte[] {1, 2, 3}, "image/png", "logo.png"))));
		cases.add(c("ProjectImageController get", "GET", "/api/projects/project/image", Map.of(), false, true, () -> client.projectImages("project").get()));
		cases.add(c("ProjectImageController update", "PUT", "/api/projects/project/image", Map.of(), true, true, () -> client.projectImages("project").update(new ProjectImage(new byte[] {1, 2, 3}, "image/png", "logo.png"))));
		cases.add(c("ProjectImageController delete", "DELETE", "/api/projects/project/image", Map.of(), false, true, () -> client.projectImages("project").delete()));
		cases.add(c("EntityTypeController baseCreate", "POST", "/api/entities/base-types", Map.of(), true, true, () -> client.baseEntityTypes().create(type)));
		cases.add(c("EntityTypeController baseGet", "GET", "/api/entities/base-types/type", Map.of(), false, true, () -> client.baseEntityTypes().get("type")));
		cases.add(c("EntityTypeController baseSearch", "GET", "/api/entities/base-types", Map.of("query", "query"), false, true, () -> client.baseEntityTypes().search("query")));
		cases.add(c("EntityTypeController create", "POST", "/api/projects/project/entities/config", Map.of(), true, true, () -> client.entityTypes("project").create(type)));
		cases.add(c("EntityTypeController get", "GET", "/api/projects/project/entities/config/type", Map.of(), false, true, () -> client.entityTypes("project").get("type")));
		cases.add(c("EntityTypeController update", "PUT", "/api/projects/project/entities/config/type", Map.of(), true, true, () -> client.entityTypes("project").update("type", type)));
		cases.add(c("EntityTypeController delete", "DELETE", "/api/projects/project/entities/config/type", Map.of(), false, true, () -> client.entityTypes("project").delete("type")));
		cases.add(c("EntityTypeController search", "GET", "/api/projects/project/entities", Map.of("query", "query"), false, true, () -> client.entityTypes("project").search("query")));
		cases.add(c("EntityController create", "POST", "/api/projects/project/entities/type", Map.of("recordLinkageResolution", "CREATE_ORIGINAL"), true, true, () -> client.entities("project", "type").create(entity, RecordLinkageResolution.CREATE_ORIGINAL)));
		cases.add(c("EntityController get", "GET", "/api/projects/project/entities/type/" + id, Map.of(), false, true, () -> client.entities("project", "type").get(id)));
		cases.add(c("EntityController update", "PUT", "/api/projects/project/entities/type/" + id, Map.of(), true, true, () -> client.entities("project", "type").update(id, entity)));
		cases.add(c("EntityController delete", "DELETE", "/api/projects/project/entities/type/" + id, Map.of(), false, true, () -> client.entities("project", "type").delete(id)));
		cases.add(c("EntityController search", "GET", "/api/projects/project/entities/type", Map.of("query", "query"), false, true, () -> client.entities("project", "type").search("query")));
		cases.add(c("EntityController pseudonyms", "GET", "/api/projects/project/entities/type/" + id + "/pseudonyms", Map.of(), false, true, () -> client.entities("project", "type").getPseudonyms(id)));
		cases.add(c("EntityController recordLinkage", "POST", "/api/projects/project/entities/type/record-linkage", Map.of(), true, true, () -> client.entities("project", "type").findRecordLinkageCandidates(entity)));
		cases.add(c("PermissionController userSearch", "GET", "/api/permissions/users", Map.of("query", "query"), false, true, () -> client.permissions().searchUsers("query")));
		cases.add(c("PermissionController domainCreate", "POST", "/api/permissions/domains/domain", Map.of("userId", "user"), true, true, () -> client.permissions().createDomain("domain", "user", permissions)));
		cases.add(c("PermissionController projectCreate", "POST", "/api/permissions/projects/project", Map.of("userId", "user"), true, true, () -> client.permissions().createProject("project", "user", permissions)));
		cases.add(c("PermissionController globalCreate", "POST", "/api/permissions/global", Map.of("userId", "user"), true, true, () -> client.permissions().createGlobal("user", permissions)));
		cases.add(c("PermissionController typeCreate", "POST", "/api/permissions/projects/project/entity-types/type", Map.of("userId", "user"), true, true, () -> client.permissions().createEntityType("project", "type", "user", permissions)));
		cases.add(c("PermissionController domainGet", "GET", "/api/permissions/domains/domain", Map.of("userId", "user"), false, true, () -> client.permissions().getDomain("domain", "user")));
		cases.add(c("PermissionController projectGet", "GET", "/api/permissions/projects/project", Map.of("userId", "user"), false, true, () -> client.permissions().getProject("project", "user")));
		cases.add(c("PermissionController globalGet", "GET", "/api/permissions/global", Map.of("userId", "user"), false, true, () -> client.permissions().getGlobal("user")));
		cases.add(c("PermissionController typeGet", "GET", "/api/permissions/projects/project/entity-types/type", Map.of("userId", "user"), false, true, () -> client.permissions().getEntityType("project", "type", "user")));
		cases.add(c("PermissionController available", "GET", "/api/permissions", Map.of(), false, true, () -> client.permissions().getAvailable()));
		cases.add(c("PermissionController domainUpdate", "PUT", "/api/permissions/domains/domain", Map.of("userId", "user"), true, true, () -> client.permissions().updateDomain("domain", "user", permissions)));
		cases.add(c("PermissionController projectUpdate", "PUT", "/api/permissions/projects/project", Map.of("userId", "user"), true, true, () -> client.permissions().updateProject("project", "user", permissions)));
		cases.add(c("PermissionController globalUpdate", "PUT", "/api/permissions/global", Map.of("userId", "user"), true, true, () -> client.permissions().updateGlobal("user", permissions)));
		cases.add(c("PermissionController typeUpdate", "PUT", "/api/permissions/projects/project/entity-types/type", Map.of("userId", "user"), true, true, () -> client.permissions().updateEntityType("project", "type", "user", permissions)));
		cases.add(c("PermissionController domainDelete", "DELETE", "/api/permissions/domains/domain", Map.of("userId", "user"), true, true, () -> client.permissions().deleteDomain("domain", "user", permissions)));
		cases.add(c("PermissionController projectDelete", "DELETE", "/api/permissions/projects/project", Map.of("userId", "user"), true, true, () -> client.permissions().deleteProject("project", "user", permissions)));
		cases.add(c("PermissionController globalDelete", "DELETE", "/api/permissions/global", Map.of("userId", "user"), true, true, () -> client.permissions().deleteGlobal("user", permissions)));
		cases.add(c("PermissionController typeDelete", "DELETE", "/api/permissions/projects/project/entity-types/type", Map.of("userId", "user"), true, true, () -> client.permissions().deleteEntityType("project", "type", "user", permissions)));
		assertEquals(69, cases.size());
		return cases.stream().map(spec -> DynamicTest.dynamicTest(spec.name, () -> run(spec)));
	}

	private static Case c(String name, String method, String path, Map<String, String> query, boolean body, boolean result, Supplier<?> action) { return new Case(name, method, path, query, body, result, action); }
	private static void run(Case spec) {
		requests.clear(); current = spec; Object result = spec.action.get();
		assertEquals(1, requests.size()); Request request = requests.get(0);
		assertEquals(spec.method, request.method); assertEquals(spec.path, request.path); assertEquals(spec.query, query(request.query));
		assertEquals("application/json", request.accept); assertEquals("GET".equals(spec.method) && spec.path.equals("/api/health") ? null : "Bearer fake-token", request.authorization);
		if (spec.body) {
			assertTrue(request.contentType.startsWith(spec.path.endsWith("/image") ? "multipart/form-data" : "application/json"));
			if (spec.path.endsWith("/image")) {
				String multipart = new String(request.body, StandardCharsets.ISO_8859_1);
				assertTrue(multipart.contains("name=\"image\"")); assertTrue(multipart.contains("filename=\"logo.png\"")); assertTrue(multipart.contains("Content-Type: image/png"));
				assertTrue(containsBytes(request.body, new byte[] {1, 2, 3}));
			}
		} else assertFalse(request.body.length > 0);
		if (spec.result) assertNotNull(result);
	}
	private static Map<String, String> query(String raw) { Map<String, String> result = new LinkedHashMap<>(); if (raw == null) return result; for (String item : raw.split("&")) { String[] pair = item.split("=", 2); result.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), URLDecoder.decode(pair[1], StandardCharsets.UTF_8)); } return result; }
	private static boolean containsBytes(byte[] actual, byte[] expected) { for (int i = 0; i <= actual.length - expected.length; i++) if (Arrays.equals(expected, Arrays.copyOfRange(actual, i, i + expected.length))) return true; return false; }
	private static void respond(HttpExchange exchange) throws IOException {
		byte[] requestBody = exchange.getRequestBody().readAllBytes();
		requests.add(new Request(exchange.getRequestMethod(), exchange.getRequestURI().getPath(), exchange.getRequestURI().getRawQuery(), exchange.getRequestHeaders().getFirst("Authorization"), exchange.getRequestHeaders().getFirst("Accept"), exchange.getRequestHeaders().getFirst("Content-Type"), requestBody));
		int status = current.path.endsWith("/statistics") ? 501 : current.method.equals("DELETE") ? 204 : current.path.endsWith("record-linkage") ? 200 : current.method.equals("POST") ? 201 : 200;
		byte[] response = current.path.equals("/api/health") ? "{\"status\":\"UP\",\"service\":\"test\",\"timestamp\":\"2026-01-01T00:00:00Z\"}".getBytes(StandardCharsets.UTF_8) : current.path.endsWith("/name") ? "\"name\"".getBytes(StandardCharsets.UTF_8) : current.path.endsWith("/validation") ? "true".getBytes(StandardCharsets.UTF_8) : current.path.endsWith("/image") && current.method.equals("GET") ? new byte[] {1, 2, 3} : "{}".getBytes(StandardCharsets.UTF_8);
		if (current.path.endsWith("/image") && current.method.equals("GET")) exchange.getResponseHeaders().set("Content-Type", "image/png"); else exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, status == 204 ? -1 : response.length); if (status != 204) exchange.getResponseBody().write(response); exchange.close();
	}
	private record Case(String name, String method, String path, Map<String, String> query, boolean body, boolean result, Supplier<?> action) { }
	private record Request(String method, String path, String query, String authorization, String accept, String contentType, byte[] body) { }
}
