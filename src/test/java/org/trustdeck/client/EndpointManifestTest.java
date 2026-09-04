package org.trustdeck.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/** Guards the reviewed backend endpoint surface. */
class EndpointManifestTest {
	@Test
	void hasAllReviewedBusinessEndpointsAndCorrections() throws IOException {
		String manifest = Files.readString(Path.of("ENDPOINTS.md"));
		var rows = manifest.lines().filter(line -> line.startsWith("| ") && !line.startsWith("| ---")).toList();
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
