package org.trustdeck.client.model;

import lombok.Data;

/** Structured backend error status information. */
@Data
public class HttpStatusInfo {
	/** Creates an empty status model for JSON binding. */
	public HttpStatusInfo() { }
	private Integer statusCode;
	private String statusMessage;
}
