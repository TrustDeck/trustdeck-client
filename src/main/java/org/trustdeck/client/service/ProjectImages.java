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
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.ProjectImage;

/** Operations for the image of one project. */
public class ProjectImages {
	private final TrustDeckHttpClient http; private final String project;
	/** Creates the service.
	 * @param http shared HTTP transport
	 * @param project project abbreviation
	 */
	public ProjectImages(TrustDeckHttpClient http, String project) { this.http = http; this.project = TrustDeckHttpClient.require(project, "projectAbbreviation"); }
	/** Uploads a project image.
	 * @param image image data
	 * @return {@code true} when accepted
	 */
	public boolean create(ProjectImage image) { upload(HttpMethod.POST, image, Set.of(201)); return true; }
	/** Gets the project image.
	 * @return image data and response MIME type
	 */
	public ProjectImage get() { var r = http.bytes(HttpMethod.GET, http.uri(path(), Map.of()), Set.of(200), true); return new ProjectImage(r.getBody(), r.getContentType(), null); }
	/** Replaces a project image.
	 * @param image image data
	 * @return {@code true} when accepted
	 */
	public boolean update(ProjectImage image) { upload(HttpMethod.PUT, image, Set.of(200)); return true; }
	/** Deletes the project image.
	 * @return {@code true} when accepted
	 */
	public boolean delete() { http.empty(HttpMethod.DELETE, http.uri(path(), Map.of()), null, Set.of(204), true); return true; }
	private void upload(HttpMethod method, ProjectImage image, Set<Integer> statuses) {
		TrustDeckHttpClient.require(image.getMimeType(), "mimeType");
		ByteArrayResource resource = new ByteArrayResource(image.getData()) {
			@Override public String getFilename() { return image.getFilename() == null ? "image" : image.getFilename(); }
		};
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(image.getMimeType()));
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("image", new HttpEntity<>(resource, headers));
		http.multipart(method, http.uri(path(), Map.of()), body, statuses);
	}
	private String[] path() { return new String[] { "api", "projects", project, "image" }; }
}
