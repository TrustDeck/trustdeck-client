package org.trustdeck.client;

/** Supplies a bearer access token for authenticated requests. */
@FunctionalInterface
public interface AccessTokenProvider {
	/** Returns the current bearer token without the {@code Bearer } prefix.
	 * @return current access token
	 */
	String getAccessToken();
}
