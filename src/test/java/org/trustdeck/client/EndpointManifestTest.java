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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Guards the reviewed backend endpoint surface.
 *
 * @author Armin Müller
 */
class EndpointManifestTest {
	
	@Test
	void hasAllReviewedBusinessEndpointsAndCorrections() throws IOException {
		String manifest = Files.readString(Path.of("ENDPOINTS.md"));
		List<String> rows = manifest.lines().filter(line -> line.startsWith("| ") && !line.startsWith("| ---")).toList();
		
		assertEquals(69, rows.size() - 1);
		assertEquals(69, rows.stream().skip(1).map(EndpointManifestTest::identity).distinct().count());
		
		assertTrue(manifest.contains("subtree | DomainTree"));
		assertTrue(manifest.contains("single-delete | DELETE") && manifest.contains("204 only boolean"));
		assertTrue(manifest.contains("Pseudonym.domain`) is excluded") || manifest.contains("`Pseudonym.domain` is excluded"));
	}
	
	private static String identity(String row) {
		String[] columns = row.split("\\|", -1);
		return columns[2].trim() + " " + columns[3].trim() + " " + columns[4].trim();
	}
}
