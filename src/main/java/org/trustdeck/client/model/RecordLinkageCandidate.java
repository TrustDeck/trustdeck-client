package org.trustdeck.client.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/** A potential duplicate entity identified by record linkage. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecordLinkageCandidate {
	/** Creates an empty candidate model for JSON binding. */
	public RecordLinkageCandidate() { }
	/** Candidate lifecycle state. */
	public enum CandidateStatus {
		/** Candidate is active. */
		ACTIVE,
		/** Candidate is deleted. */
		DELETED
	}
	/** Resolution selected when creating an entity with candidates. */
	public enum RecordLinkageResolution {
		/** Create the entity as an original record. */
		CREATE_ORIGINAL
	}
	private Entity entity;
	private Double score;
	private Double normalizedScore;
	private List<String> matchedOn;
	private CandidateStatus candidateStatus;
}
