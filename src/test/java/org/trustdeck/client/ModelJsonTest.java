package org.trustdeck.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.trustdeck.client.model.HealthStatus;

/** Basic shared-mapper compatibility coverage. */
class ModelJsonTest {
	@Test
	void parsesJavaTimeAndUnknownFields() throws Exception {
		TrustDeckHttpClient client = new TrustDeckHttpClient("https://example.invalid", () -> "token");
		HealthStatus health = client.mapper().readValue("{\"status\":\"UP\",\"service\":\"backend\",\"timestamp\":\"2026-01-01T00:00:00Z\",\"newField\":true}", HealthStatus.class);
		assertEquals("UP", health.getStatus());
		assertEquals(OffsetDateTime.parse("2026-01-01T00:00:00Z"), health.getTimestamp());
	}
}
