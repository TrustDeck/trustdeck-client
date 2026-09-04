package org.trustdeck.client.exception;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.trustdeck.client.model.HttpStatusInfo;
import org.trustdeck.client.model.RecordLinkageCandidate;

/** Indicates that entity creation found record-linkage candidates. */
public class RecordLinkageConflictException extends TrustDeckResponseException {
	private static final long serialVersionUID = 1L;
	/** Candidates returned by record linkage. */
	private final List<RecordLinkageCandidate> candidates;

	/** Creates a conflict containing the backend candidates.
	 * @param status HTTP status
	 * @param info parsed backend status information
	 * @param rawBody bounded raw response body
	 * @param contentType response content type
	 * @param candidates record-linkage candidates
	 */
	public RecordLinkageConflictException(HttpStatusCode status, HttpStatusInfo info, String rawBody, String contentType, List<RecordLinkageCandidate> candidates) {
		super("Entity creation requires record-linkage resolution.", status, info, rawBody, contentType, null);
		this.candidates = List.copyOf(candidates);
	}

	/** Returns the candidates that caused the conflict.
	 * @return immutable candidate list
	 */
	public List<RecordLinkageCandidate> getCandidates() { return candidates; }
}
