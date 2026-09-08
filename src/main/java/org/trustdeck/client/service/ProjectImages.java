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

package org.trustdeck.client.service;

import java.util.Map;
import java.util.Set;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.trustdeck.client.Response;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.ProjectImage;

/**
 * Operations for the image of one project.
 *
 * @author Armin Müller
 */
public class ProjectImages {
	
	/** Shared HTTP transport object. */
	private final TrustDeckHttpClient http;
	
	/** The abbreviation to uniquely identify a project. */
	private final String projectAbbreviation;
	
	/**
	 * Creates the service.
	 * 
	 * @param http shared HTTP transport object
	 * @param projectAbbreviation the project's abbreviation
	 */
	public ProjectImages(TrustDeckHttpClient http, String projectAbbreviation) {
		this.http = http;
		this.projectAbbreviation = TrustDeckHttpClient.require(projectAbbreviation, "projectAbbreviation");
	}

	/**
	 * Uploads a project image.
	 * 
	 * @param image image data
	 * @return {@code true} when accepted
	 */
	public boolean create(ProjectImage image) {
		upload(HttpMethod.POST, image, Set.of(201));

		return true;
	}

	/**
	 * Gets the project image.
	 * 
	 * @return image data and response MIME type
	 */
	public ProjectImage get() {
		Response<byte[]> response = http.bytes(HttpMethod.GET, http.uri(path(), Map.of()), Set.of(200), true);

		return new ProjectImage(response.getBody(), response.getContentType(), null);
	}

	/**
	 * Replaces a project image.
	 * 
	 * @param image image data
	 * @return {@code true} when accepted
	 */
	public boolean update(ProjectImage image) {
		upload(HttpMethod.PUT, image, Set.of(200));

		return true;
	}

	/**
	 * Deletes the project image.
	 * 
	 * @return {@code true} when accepted
	 */
	public boolean delete() {
		http.empty(HttpMethod.DELETE, http.uri(path(), Map.of()), null, Set.of(204), true);

		return true;
	}

	/**
	 * Uploads a project image as multipart form data.
	 *
	 * @param method HTTP method to use
	 * @param image image data and metadata to upload
	 * @param statuses acceptable HTTP response status codes
	 * @throws IllegalArgumentException if the image MIME type is {@code null} or blank
	 */
	private void upload(HttpMethod method, ProjectImage image, Set<Integer> statuses) {
		TrustDeckHttpClient.require(image.getMimeType(), "mimeType");
		
		ByteArrayResource resource = new ByteArrayResource(image.getData()) {
			/** Returns the caller-provided filename or the transport fallback. */
			@Override
			public String getFilename() {
				return image.getFilename() == null ? "image" : image.getFilename();
			}
		};

		// Set the image type header
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(image.getMimeType()));

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("image", new HttpEntity<>(resource, headers));

		http.multipart(method, http.uri(path(), Map.of()), body, statuses);
	}

	/**
	 * Builds the endpoint path for the current project's image resource.
	 *
	 * @return the project image endpoint path segments
	 */
	private String[] path() {
		return new String[] {"api", "projects", projectAbbreviation, "image"};
	}
}
