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
import org.springframework.web.client.RestClient.RequestBodySpec;
import org.springframework.web.util.UriComponentsBuilder;
import org.trustdeck.client.exception.TrustDeckClientLibraryException;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.HttpStatusInfo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * Shared synchronous HTTP transport for the client services.
 *
 * @author Armin Müller
 */
@Slf4j
public final class TrustDeckHttpClient {

	/** Maximum number of error-body characters retained in exceptions. */
	private static final int MAX_ERROR_BODY_LENGTH = 16_384;

	/** Spring HTTP client. */
	private final RestClient restClient;

	/** JSON mapper used for response conversion. */
	private final ObjectMapper mapper;

	/** Provider used to obtain bearer tokens. */
	private final AccessTokenProvider tokenProvider;

	/** Base URI for all requests. */
	private final URI baseUri;

	/**
	 * Creates a transport for a service URL and token provider.
	 *
	 * @param serviceUrl TrustDeck service base URL
	 * @param tokenProvider provider for authenticated requests
	 * @throws TrustDeckClientLibraryException if the URL is invalid
	 */
	public TrustDeckHttpClient(String serviceUrl, AccessTokenProvider tokenProvider) {
		try {
			baseUri = UriComponentsBuilder.fromUriString(require(serviceUrl, "serviceUrl")).build().toUri();

			// Remove trailing slashes before creating the client from the given URI
			restClient = RestClient.builder().baseUrl(baseUri.toString().replaceAll("/$", "")).build();
			mapper = new ObjectMapper().registerModule(new JavaTimeModule())
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			this.tokenProvider = tokenProvider;
		} catch (RuntimeException e) {
			throw new TrustDeckClientLibraryException("Invalid TrustDeck client configuration.", e);
		}
	}

	/**
	 * Builds an encoded URI from path segments and non-null query parameters.
	 *
	 * @param segments path segments
	 * @param parameters query parameters
	 * @return encoded request URI
	 */
	public URI uri(String[] segments, Map<String, ?> parameters) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri);
		for (String segment : segments) {
			builder.pathSegment(segment);
		}

		// Map parameters into the URI
		parameters.forEach((key, value) -> {
			if (value != null) {
				builder.queryParam(key, value);
			}
		});

		return builder.build().encode().toUri();
	}

	/**
	 * Executes a request and converts its response to a class.
	 *
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

	/**
	 * Executes a request and converts a generic response.
	 *
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

	/**
	 * Executes a request preserving response bytes.
	 *
	 * @param method HTTP method
	 * @param uri encoded URI
	 * @param expected accepted status codes
	 * @param authenticated whether to send a bearer token
	 * @return binary response
	 */
	public Response<byte[]> bytes(HttpMethod method, URI uri, Set<Integer> expected, boolean authenticated) {
		return exchange(method, uri, null, byte[].class, null, expected, authenticated, null);
	}

	/**
	 * Executes a request whose public result has no body.
	 *
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

	/**
	 * Executes an authenticated multipart request.
	 *
	 * @param method HTTP method
	 * @param uri encoded URI
	 * @param body multipart body
	 * @param expected accepted status codes
	 * @return response metadata
	 */
	public Response<Void> multipart(HttpMethod method, URI uri, Object body, Set<Integer> expected) {
		return exchange(method, uri, body, Void.class, null, expected, true, MediaType.MULTIPART_FORM_DATA);
	}

	/**
	 * Executes a request and converts its response using either a class or generic type.
	 *
	 * @param <T> response type
	 * @param method HTTP method
	 * @param uri encoded URI
	 * @param body optional request body
	 * @param classType response class
	 * @param genericType generic response type
	 * @param expected accepted status codes
	 * @param authenticated whether to send a bearer token
	 * @param contentType type of the body
	 * @return the response containing the status code, content type, location, deserialized body, and raw response bytes
	 */
	private <T> Response<T> exchange(HttpMethod method, URI uri, Object body, Class<T> classType,
			ParameterizedTypeReference<T> genericType, Set<Integer> expected, boolean authenticated, MediaType contentType) {

		try {
			RequestBodySpec request = restClient.method(method).uri(uri).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

			// Add Bearer token
			if (authenticated) {
				request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token());
			}

			// Add body and body's content type when applicable
			if (body != null) {
				if (contentType != null) {
					request.contentType(contentType);
				} else {
					request.contentType(MediaType.APPLICATION_JSON);
				}

				request.body(body);
			}

			// Execute the request, validate the response status, and deserialize the response body into the expected type
			log.trace("Executing TrustDeck {} request (authenticated: {}, body present: {}).", method, authenticated,
					body != null);
			return request.exchange((requestHeaders, response) -> {
				// Extract info from response
				byte[] bytes = response.getBody().readAllBytes();
				HttpStatusCode status = response.getStatusCode();
				String content = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
				String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				log.trace("Received TrustDeck response (method: {}, status: {}, body size: {} bytes).", method,
						status.value(), bytes.length);

				// Check if we expected the status code that was returned
				if (!expected.contains(status.value())) {
					// No, we didn't
					log.debug("Unexpected TrustDeck response status: {}. Expected: {}.", status.value(), expected);
					throw responseException(status, bytes, content, location);
				}

				// Parse returned body into the proper content object
				T value = null;
				if (classType == byte[].class) {
					@SuppressWarnings("unchecked") T raw = (T) bytes;
					value = raw;
				} else if (classType != null && classType != Void.class && bytes.length > 0) {
					value = mapper.readValue(bytes, classType);
				}

				// Deserialize a non-empty response, treating an empty JSON object ("{}") as an empty list
				if (genericType != null && bytes.length > 0) {
					String text = new String(bytes, StandardCharsets.UTF_8).trim();
					@SuppressWarnings("unchecked") T emptyList = (T) List.of();

					if ("{}".equals(text)) {
						log.trace("Interpreting an empty JSON object as an empty list.");
						value = emptyList;
					} else {
						value = mapper.readValue(bytes, mapper.constructType(genericType.getType()));
					}
				}

				return new Response<>(status.value(), content, location, value, bytes);
			});
		} catch (TrustDeckResponseException e) {
			throw e;
		} catch (Exception e) {
			log.debug("TrustDeck {} request failed (exception type: {}).", method, e.getClass().getSimpleName());
			throw new TrustDeckClientLibraryException("TrustDeck request failed.", e);
		}
	}

	/**
	 * Creates an exception containing the response details and diagnostic content.
	 *
	 * @param status the HTTP response status
	 * @param body the response body
	 * @param contentType the response content type
	 * @param location the response location header
	 * @return the created response exception
	 */
	private TrustDeckResponseException responseException(HttpStatusCode status, byte[] body, String contentType, String location) {
		String raw = new String(body, StandardCharsets.UTF_8);
		if (raw.length() > MAX_ERROR_BODY_LENGTH) {
			raw = raw.substring(0, MAX_ERROR_BODY_LENGTH);
		}

		HttpStatusInfo info = null;
		try {
			info = mapper.readValue(raw, HttpStatusInfo.class);
		} catch (Exception ignored) {
			// Preserve the raw response when it is not a status-info document
		}

		return new TrustDeckResponseException("TrustDeck returned HTTP " + status.value() + ".", status, info, raw, contentType, location);
	}

	/**
	 * Returns the configured JSON mapper used by response conversion.
	 *
	 * @return JSON mapper
	 */
	public ObjectMapper mapper() {
		return mapper;
	}

	/**
	 * Require a non-blank string.
	 *
	 * @param value value to validate
	 * @param name logical value name
	 * @return the original value
	 * @throws IllegalArgumentException if the value is null or blank
	 */
	public static String require(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank.");
		}

		return value;
	}

	/**
	 * Obtains and validates an access token for an authenticated request.
	 *
	 * @return the access token
	 * @throws TrustDeckClientLibraryException if no token could be acquired
	 */
	private String token() {
		try {
			return require(tokenProvider.getAccessToken(), "access token");
		} catch (RuntimeException exception) {
			throw new TrustDeckClientLibraryException("Acquiring an access token failed.", exception);
		}
	}

}
