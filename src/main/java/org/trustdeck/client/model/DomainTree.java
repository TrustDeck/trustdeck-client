package org.trustdeck.client.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** A domain and its nested children. */
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainTree {
	/** Creates an empty tree model for JSON binding. */
	public DomainTree() { }
	private Domain domain;
	private List<DomainTree> children;
}
