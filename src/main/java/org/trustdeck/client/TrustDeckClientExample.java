package org.trustdeck.client;

import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.model.Entity;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Minimal example using configuration supplied outside source control. */
public class TrustDeckClientExample {
	/** Creates the example entry point. */
	public TrustDeckClientExample() { }
	/** Runs the environment-configured health example.
	 * @param args command-line arguments, unused
	 */
	public static void main(String[] args) {
		TrustDeckClientConfig config = TrustDeckClientConfig.builder()
				.serviceUrl(value("TRUSTDECK_SERVICE_URL"))
				.keycloakUrl(value("TRUSTDECK_KEYCLOAK_URL"))
				.realm(value("TRUSTDECK_REALM"))
				.clientId(value("TRUSTDECK_CLIENT_ID"))
				.clientSecret(value("TRUSTDECK_CLIENT_SECRET"))
				.userName(value("TRUSTDECK_USERNAME"))
				.password(value("TRUSTDECK_PASSWORD"))
				.build();
		TrustDeckClient client = new TrustDeckClient(config);
		Entity entity = new Entity();
		entity.setData(new ObjectMapper().valueToTree(java.util.Map.of("example", "value")));
		System.out.println(client.health().getStatus());
	}

	private static String value(String key) {
		String value = System.getProperty(key);
		return value == null ? System.getenv(key) : value;
	}
}
