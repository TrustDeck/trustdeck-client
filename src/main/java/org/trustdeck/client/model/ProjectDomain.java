package org.trustdeck.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/** A concise domain view owned by a project. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDomain {
	/** Creates an empty project-domain model for JSON binding. */
	public ProjectDomain() { }
	private String name;
	private String prefix;
	private String projectAbbreviation;
	private String superDomainName;
}
