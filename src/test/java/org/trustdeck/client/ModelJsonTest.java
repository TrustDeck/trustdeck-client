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

import java.time.Instant;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.trustdeck.client.model.HealthStatus;
import org.trustdeck.client.model.ProjectStatistics;
import org.trustdeck.client.model.SystemStatistics;

/**
 * Tests JSON serialization and deserialization behavior of model classes.
 *
 * @author Armin Müller
 */
class ModelJsonTest {
	
	/**
	 * Verifies that Java time values are parsed correctly and unknown JSON fields
	 * are ignored during deserialization.
	 *
	 * @throws Exception if JSON deserialization fails
	 */
	@Test
	void parsesJavaTimeAndUnknownFields() throws Exception {
		TrustDeckHttpClient client = new TrustDeckHttpClient("https://example.invalid", () -> "token");
		HealthStatus health = client.mapper().readValue("{\"status\":\"UP\",\"service\":\"backend\",\"timestamp\":\"2026-01-01T00:00:00Z\",\"newField\":true}", HealthStatus.class);
		
		assertEquals("UP", health.getStatus());
		assertEquals(OffsetDateTime.parse("2026-01-01T00:00:00Z"), health.getTimestamp());
	}

	/**
	 * Verifies every project statistics field, including nullable validity.
	 *
	 * @throws Exception if JSON deserialization fails
	 */
	@Test
	void parsesProjectStatistics() throws Exception {
		TrustDeckHttpClient client = new TrustDeckHttpClient("https://example.invalid", () -> "token");
		String json = "{\"generatedAt\":\"2026-02-03T04:05:06Z\",\"project\":{\"name\":\"Study\",\"abbreviation\":\"ST\",\"startDate\":\"2025-01-02T03:04:05+01:00\",\"endDate\":\"2027-06-07T08:09:10-04:00\",\"remainingValiditySeconds\":9876},\"counts\":{\"domains\":11,\"entityTypes\":22,\"entities\":33,\"pseudonyms\":44},\"entitiesByType\":[{\"name\":\"person\",\"count\":55},{\"name\":\"visit\",\"count\":66}],\"pseudonymsByDomain\":[{\"name\":\"email\",\"count\":77},{\"name\":\"phone\",\"count\":88}]}";
		ProjectStatistics statistics = client.mapper().readValue(json, ProjectStatistics.class);

		assertEquals(Instant.parse("2026-02-03T04:05:06Z"), statistics.getGeneratedAt());
		assertEquals("Study", statistics.getProject().getName());
		assertEquals("ST", statistics.getProject().getAbbreviation());
		assertEquals(OffsetDateTime.parse("2025-01-02T03:04:05+01:00"), statistics.getProject().getStartDate());
		assertEquals(OffsetDateTime.parse("2027-06-07T08:09:10-04:00"), statistics.getProject().getEndDate());
		assertEquals(9876L, statistics.getProject().getRemainingValiditySeconds());
		assertEquals(11L, statistics.getCounts().getDomains());
		assertEquals(22L, statistics.getCounts().getEntityTypes());
		assertEquals(33L, statistics.getCounts().getEntities());
		assertEquals(44L, statistics.getCounts().getPseudonyms());
		assertEquals("person", statistics.getEntitiesByType().get(0).getName());
		assertEquals(66L, statistics.getEntitiesByType().get(1).getCount());
		assertEquals("email", statistics.getPseudonymsByDomain().get(0).getName());
		assertEquals(88L, statistics.getPseudonymsByDomain().get(1).getCount());

		ProjectStatistics.ProjectDetails details = client.mapper().readValue("{\"name\":\"No end\",\"remainingValiditySeconds\":null}", ProjectStatistics.ProjectDetails.class);
		assertNull(details.getRemainingValiditySeconds());
	}

	/**
	 * Verifies every system statistics field and boolean mapping.
	 *
	 * @throws Exception if JSON deserialization fails
	 */
	@Test
	void parsesSystemStatistics() throws Exception {
		TrustDeckHttpClient client = new TrustDeckHttpClient("https://example.invalid", () -> "token");
		String json = "{\"application\":{\"version\":\"2.3.0\",\"buildCommit\":\"0123456789abcdef0123456789abcdef01234567\"},\"database\":{\"databaseTime\":\"2026-03-04T05:06:07+02:00\",\"sizeBytes\":101,\"tables\":[{\"schemaName\":\"public\",\"tableName\":\"audit_event\",\"recordCount\":202,\"recordCountExact\":true,\"tableSizeBytes\":303,\"indexSizeBytes\":404,\"toastSizeBytes\":505,\"totalRelationSizeBytes\":606},{\"schemaName\":\"public\",\"tableName\":\"entity\",\"recordCount\":707,\"recordCountExact\":false,\"tableSizeBytes\":808,\"indexSizeBytes\":909,\"toastSizeBytes\":1001,\"totalRelationSizeBytes\":1102}]},\"connectionPool\":{\"activeConnections\":12,\"idleConnections\":13,\"totalConnections\":14,\"threadsAwaitingConnection\":15,\"maximumPoolSize\":16,\"minimumIdleConnections\":17},\"jvmMemory\":{\"heapUsedBytes\":18,\"heapCommittedBytes\":19,\"heapMaximumBytes\":20,\"nonHeapUsedBytes\":21,\"nonHeapCommittedBytes\":22}}";
		SystemStatistics statistics = client.mapper().readValue(json, SystemStatistics.class);

		assertEquals("2.3.0", statistics.getApplication().getVersion());
		assertEquals("0123456789abcdef0123456789abcdef01234567", statistics.getApplication().getBuildCommit());
		assertEquals(OffsetDateTime.parse("2026-03-04T05:06:07+02:00"), statistics.getDatabase().getDatabaseTime());
		assertEquals(101L, statistics.getDatabase().getSizeBytes());
		SystemStatistics.TableInfo exact = statistics.getDatabase().getTables().get(0);
		assertEquals("public", exact.getSchemaName());
		assertEquals("audit_event", exact.getTableName());
		assertEquals(202L, exact.getRecordCount());
		assertEquals(true, exact.isRecordCountExact());
		assertEquals(303L, exact.getTableSizeBytes());
		assertEquals(404L, exact.getIndexSizeBytes());
		assertEquals(505L, exact.getToastSizeBytes());
		assertEquals(606L, exact.getTotalRelationSizeBytes());
		SystemStatistics.TableInfo estimated = statistics.getDatabase().getTables().get(1);
		assertEquals(false, estimated.isRecordCountExact());
		assertEquals(707L, estimated.getRecordCount());
		assertEquals(808L, estimated.getTableSizeBytes());
		assertEquals(909L, estimated.getIndexSizeBytes());
		assertEquals(1001L, estimated.getToastSizeBytes());
		assertEquals(1102L, estimated.getTotalRelationSizeBytes());
		assertEquals(12, statistics.getConnectionPool().getActiveConnections());
		assertEquals(13, statistics.getConnectionPool().getIdleConnections());
		assertEquals(14, statistics.getConnectionPool().getTotalConnections());
		assertEquals(15, statistics.getConnectionPool().getThreadsAwaitingConnection());
		assertEquals(16, statistics.getConnectionPool().getMaximumPoolSize());
		assertEquals(17, statistics.getConnectionPool().getMinimumIdleConnections());
		assertEquals(18L, statistics.getJvmMemory().getHeapUsedBytes());
		assertEquals(19L, statistics.getJvmMemory().getHeapCommittedBytes());
		assertEquals(20L, statistics.getJvmMemory().getHeapMaximumBytes());
		assertEquals(21L, statistics.getJvmMemory().getNonHeapUsedBytes());
		assertEquals(22L, statistics.getJvmMemory().getNonHeapCommittedBytes());
	}
}
