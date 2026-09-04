package org.trustdeck.client;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.client.exception.TrustDeckClientLibraryException;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.HttpStatusInfo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** Shared synchronous HTTP transport for the client services. */
public final class TrustDeckHttpClient {
	private static final int MAX_ERROR_BODY_LENGTH = 16_384;
	private final RestClient restClient;
	private final ObjectMapper mapper;
	private final AccessTokenProvider tokenProvider;
	private final URI baseUri;

	/** Creates a transport for a service URL and token provider.
	 * @param serviceUrl TrustDeck service base URL
	 * @param tokenProvider provider for authenticated requests
	 * @throws TrustDeckClientLibraryException if the URL is invalid
	 */
	public TrustDeckHttpClient(String serviceUrl, AccessTokenProvider tokenProvider) {
		try {
			baseUri = UriComponentsBuilder.fromUriString(require(serviceUrl, "serviceUrl")).build().toUri();
			restClient = RestClient.builder().baseUrl(baseUri.toString().replaceAll("/$", "")).build();
			mapper = new ObjectMapper().registerModule(new JavaTimeModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			this.tokenProvider = tokenProvider;
		} catch (RuntimeException e) {
			throw new TrustDeckClientLibraryException("Invalid TrustDeck client configuration.", e);
		}
	}

	/** Builds an encoded URI from path segments and non-null query parameters.
	 * @param segments path segments
	 * @param parameters query parameters
	 * @return encoded request URI
	 */
	public URI uri(String[] segments, Map<String, ?> parameters) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri);
		for (String segment : segments) builder.pathSegment(segment);
		parameters.forEach((key, value) -> { if (value != null) builder.queryParam(key, value); });
		return builder.build().encode().toUri();
	}

	/** Executes a request and converts its response to a class.
	 * @param <T> response type
	 * @param method HTTP method
	 * @param uri encoded URI
	 * @param body optional request body
	 * @param type response class
	 * @param expected accepted status codes
	 * @param authenticated whether to send a bearer token
	 * @return response wrapper
	 */
	public <T> Response<T> exchange(HttpMethod method, URI uri, Object body, Class<T> type, Set<Integer> expected, boolean authenticated) {
		return exchange(method, uri, body, type, null, expected, authenticated, null);
	}

	/** Executes a request and converts a generic response.
	 * @param <T> response type
	 * @param method HTTP method
	 * @param uri encoded URI
	 * @param body optional request body
	 * @param type generic response type
	 * @param expected accepted status codes
	 * @param authenticated whether to send a bearer token
	 * @return response wrapper
	 */
	public <T> Response<T> exchange(HttpMethod method, URI uri, Object body, ParameterizedTypeReference<T> type, Set<Integer> expected, boolean authenticated) {
		return exchange(method, uri, body, null, type, expected, authenticated, null);
	}

	/** Executes a request preserving response bytes.
	 * @param method HTTP method
	 * @param uri encoded URI
	 * @param expected accepted status codes
	 * @param authenticated whether to send a bearer token
	 * @return binary response
	 */
	public Response<byte[]> bytes(HttpMethod method, URI uri, Set<Integer> expected, boolean authenticated) {
		return exchange(method, uri, null, byte[].class, null, expected, authenticated, null);
	}

	/** Executes a request whose public result has no body.
	 * @param method HTTP method
	 * @param uri encoded URI
	 * @param body optional request body
	 * @param expected accepted status codes
	 * @param authenticated whether to send a bearer token
	 * @return response metadata
	 */
	public Response<Void> empty(HttpMethod method, URI uri, Object body, Set<Integer> expected, boolean authenticated) {
		return exchange(method, uri, body, Void.class, null, expected, authenticated, null);
	}

	/** Executes an authenticated multipart request.
	 * @param method HTTP method
	 * @param uri encoded URI
	 * @param body multipart body
	 * @param expected accepted status codes
	 * @return response metadata
	 */
	public Response<Void> multipart(HttpMethod method, URI uri, Object body, Set<Integer> expected) {
		return exchange(method, uri, body, Void.class, null, expected, true, MediaType.MULTIPART_FORM_DATA);
	}

	private <T> Response<T> exchange(HttpMethod method, URI uri, Object body, Class<T> classType, ParameterizedTypeReference<T> genericType, Set<Integer> expected, boolean authenticated, MediaType contentType) {
		try {
			RestClient.RequestBodySpec request = restClient.method(method).uri(uri).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
			if (authenticated) request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token());
			if (body != null) {
				if (contentType != null) request.contentType(contentType);
				else request.contentType(MediaType.APPLICATION_JSON);
				request.body(body);
			}
			return request.exchange((requestHeaders, response) -> {
				byte[] bytes = response.getBody().readAllBytes();
				HttpStatusCode status = response.getStatusCode();
				String content = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
				String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				if (!expected.contains(status.value())) throw responseException(status, bytes, content, location);
				T value = null;
				if (classType == byte[].class) {
					@SuppressWarnings("unchecked") T raw = (T) bytes;
					value = raw;
				} else if (classType != null && classType != Void.class && bytes.length > 0) value = mapper.readValue(bytes, classType);
				if (genericType != null && bytes.length > 0) {
					String text = new String(bytes, StandardCharsets.UTF_8).trim();
					@SuppressWarnings("unchecked") T emptyList = (T) List.of();
					value = "{}".equals(text) ? emptyList : mapper.readValue(bytes, mapper.constructType(genericType.getType()));
				}
				return new Response<>(status.value(), content, location, value, bytes);
			});
		} catch (TrustDeckResponseException e) {
			throw e;
		} catch (Exception e) {
			throw new TrustDeckClientLibraryException("TrustDeck request failed.", e);
		}
	}

	private TrustDeckResponseException responseException(HttpStatusCode status, byte[] body, String contentType, String location) {
		String raw = new String(body, StandardCharsets.UTF_8);
		if (raw.length() > MAX_ERROR_BODY_LENGTH) raw = raw.substring(0, MAX_ERROR_BODY_LENGTH);
		HttpStatusInfo info = null;
		try { info = mapper.readValue(raw, HttpStatusInfo.class); } catch (Exception ignored) { }
		return new TrustDeckResponseException("TrustDeck returned HTTP " + status.value() + ".", status, info, raw, contentType, location);
	}

	/** Returns the configured JSON mapper used by response conversion.
	 * @return JSON mapper
	 */
	public ObjectMapper mapper() { return mapper; }
	/** Requires a non-blank string.
	 * @param value value to validate
	 * @param name logical value name
	 * @return the original value
	 * @throws IllegalArgumentException if the value is null or blank
	 */
	public static String require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank."); return value; }
	/** Obtains and validates an access token for an authenticated request. */
	private String token() { try { return require(tokenProvider.getAccessToken(), "access token"); } catch (RuntimeException e) { throw new TrustDeckClientLibraryException("Acquiring an access token failed.", e); } }

	/** Response metadata, converted body, and raw bytes.
	 * @param <T> converted body type
	 */
	public static final class Response<T> {
		final int status; final String contentType; final String location; final T body; final byte[] rawBody;
		/** Creates a response wrapper. */
		Response(int status, String contentType, String location, T body, byte[] rawBody) { this.status = status; this.contentType = contentType; this.location = location; this.body = body; this.rawBody = rawBody; }
		/** Returns the HTTP status code.
		 * @return HTTP status code
		 */
		public int getStatus() { return status; }
		/** Returns the response content type.
		 * @return response content type, if supplied
		 */
		public String getContentType() { return contentType; }
		/** Returns the response location.
		 * @return response location, if supplied
		 */
		public String getLocation() { return location; }
		/** Returns the converted response body.
		 * @return converted response body
		 */
		public T getBody() { return body; }
		/** Returns the unmodified response bytes.
		 * @return unmodified response bytes
		 */
		public byte[] getRawBody() { return rawBody; }
	}
}
