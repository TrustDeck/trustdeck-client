package org.trustdeck.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Raw image data associated with a project. */
@Data
@AllArgsConstructor
public class ProjectImage {
	/** Creates an empty image model for JSON binding. */
	public ProjectImage() { }
	private byte[] data;
	private String mimeType;
	private String filename;
}
