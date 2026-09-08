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

package org.trustdeck.client.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * A potential duplicate entity identified by record linkage.
 *
 * @author Armin Müller
 */
@Data
@Builder
@AllArgsConstructor
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
	
	/** Resolution strategy selected when creating an entity with candidates. */
	public enum RecordLinkageResolutionStrategy {
		
		/** Create the entity as an original record. */
		CREATE_ORIGINAL
	}

	/** Entity returned as a possible record-linkage match. */
	private Entity entity;
	
	/** Raw candidate match score. */
	private Double score;
	
	/** Normalized candidate match score. */
	private Double normalizedScore;
	
	/** Fields that contributed to the match. */
	private List<String> matchedOn;
	
	/** Candidate lifecycle status. */
	private CandidateStatus candidateStatus;
}
