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

import java.time.OffsetDateTime;

import org.trustdeck.client.model.Project;
import org.trustdeck.client.model.ProjectStatistics;
import org.trustdeck.client.service.Domains;
import org.trustdeck.client.service.Entities;
import org.trustdeck.client.service.EntityTypes;
import org.trustdeck.client.service.Permissions;
import org.trustdeck.client.service.ProjectImages;
import org.trustdeck.client.service.ProjectPermissions;
import org.trustdeck.client.service.Projects;
import org.trustdeck.client.service.Pseudonyms;
import org.trustdeck.client.service.DomainPermissions;
import org.trustdeck.client.service.EntityTypePermissions;
import org.trustdeck.client.service.ProjectDomains;

/**
 * Provides immutable access to operations scoped to a selected TrustDeck
 * project.
 * Creating a project scope does not perform an HTTP request. The scope remains
 * bound to the project abbreviation supplied during its construction.
 * 
 * @author Armin Müller
 */
public final class ProjectScope {

	/** Shared HTTP client used for project-scoped requests. */
	private final TrustDeckHttpClient http;

	/** Project service used for operations on the selected project. */
	private final Projects projects;

	/** Global domain service used for unfiltered domain operations. */
	private final Domains globalDomains;

	/** Permission service used to create project-scoped permission services. */
	private final Permissions permissions;

	/** Abbreviation of the project represented by this scope. */
	private final String projectAbbreviation;

	/**
	 * Creates an immutable project context.
	 * 
	 * @param http shared HTTP transport
	 * @param projects project collection service
	 * @param globalDomains global domain service
	 * @param permissions global permission service
	 * @param projectAbbreviation captured project abbreviation
	 */
	public ProjectScope(TrustDeckHttpClient http, Projects projects, Domains globalDomains, Permissions permissions, String projectAbbreviation) {
		this.http = http;
		this.projects = projects;
		this.globalDomains = globalDomains;
		this.permissions = permissions;
		this.projectAbbreviation = TrustDeckHttpClient.require(projectAbbreviation, "projectAbbreviation");
	}

	/**
	 * Retrieves the project captured by this scope.
	 * @return captured project
	 */
	public Project get() {
		return projects.get(projectAbbreviation);
	}

	/**
	 * Looks up another project without rebinding this scope. Later calls to
	 * {@link #get()} and child services continue using the captured abbreviation.
	 * 
	 * @param otherAbbreviation abbreviation to look up
	 * @return looked-up project
	 */
	public Project get(String otherAbbreviation) {
		return projects.get(TrustDeckHttpClient.require(otherAbbreviation, "projectAbbreviation"));
	}

	/**
	 * Updates the captured project.
	 * 
	 * @param project replacement project
	 * @return updated project
	 */
	public Project update(Project project) {
		return projects.updateScoped(projectAbbreviation, project);
	}

	/**
	 * Deletes the captured project immediately.
	 * 
	 * @return {@code true} when accepted
	 */
	public boolean delete() {
		return projects.deleteScoped(projectAbbreviation);
	}

	/**
	 * Deletes the captured project at the supplied date, or immediately for null.
	 * 
	 * @param deleteDate scheduled deletion date, or {@code null}
	 * @return {@code true} when accepted
	 */
	public boolean delete(OffsetDateTime deleteDate) {
		return projects.deleteScoped(projectAbbreviation, deleteDate);
	}

	/**
	 * Retrieves statistics for the captured project.
	 * 
	 * @return typed project statistics
	 */
	public ProjectStatistics getStatistics() {
		return projects.getStatisticsScoped(projectAbbreviation);
	}

	/**
	 * Returns project image operations.
	 * 
	 * @return project image operations
	 */
	public ProjectImages image() {
		return new ProjectImages(http, projectAbbreviation);
	}

	/**
	 * Returns project-domain operations.
	 * 
	 * @return project-domain operations
	 */
	public ProjectDomains domains() {
		return new ProjectDomains(http, globalDomains, projectAbbreviation);
	}

	/**
	 * Returns project entity-type operations.
	 * 
	 * @return project entity-type operations
	 */
	public EntityTypes entityTypes() {
		return new EntityTypes(http, projectAbbreviation);
	}

	/**
	 * Returns entity operations for one entity type.
	 * 
	 * @param entityTypeName entity type name
	 * @return entity operations
	 */
	public Entities entities(String entityTypeName) {
		return new Entities(http, projectAbbreviation, entityTypeName);
	}

	/**
	 * Returns pseudonym operations for a domain name.
	 * 
	 * @param domainName domain name
	 * @return pseudonym operations
	 */
	public Pseudonyms pseudonyms(String domainName) {
		return new Pseudonyms(http, domainName);
	}

	/**
	 * Returns project permission operations.
	 * 
	 * @return project permission operations
	 */
	public ProjectPermissions permissions() {
		return new ProjectPermissions(permissions, projectAbbreviation);
	}

	/**
	 * Returns domain permission operations without an ownership lookup.
	 * 
	 * @param domainName domain name
	 * @return domain permission operations
	 */
	public DomainPermissions domainPermissions(String domainName) {
		return new DomainPermissions(permissions, domainName);
	}

	/**
	 * Returns entity-type permission operations without an ownership lookup.
	 * 
	 * @param typeName entity type name
	 * @return entity-type permission operations
	 */
	public EntityTypePermissions entityTypePermissions(String typeName) {
		return new EntityTypePermissions(permissions, projectAbbreviation, typeName);
	}
}
