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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.trustdeck.client.TrustDeckHttpClient;
import org.trustdeck.client.model.Algorithm;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.DomainTree;
import org.trustdeck.client.model.ProjectDomain;
import org.trustdeck.client.model.SearchResult;

/**
 * Provides domain operations within the context of a selected TrustDeck project.
 * Operations backed by global domain endpoints do not prove or enforce ownership 
 * by the selected project.
 * 
 * @author Armin Müller
 */
public final class ProjectDomains {

	/** Shared HTTP client used for domain requests. */
	private final TrustDeckHttpClient http;

	/** Global domain service used for unfiltered domain operations. */
	private final Domains globalDomains;

	/** Abbreviation of the project represented by this domain scope. */
	private final String projectAbbreviation;

	/**
	 * Creates project-domain operations without making a request.
	 * 
	 * @param http shared HTTP transport
	 * @param globalDomains global domain service used for search delegation
	 * @param projectAbbreviation selected project abbreviation
	 */
	public ProjectDomains(TrustDeckHttpClient http, Domains globalDomains, String projectAbbreviation) {
		this.http = http;
		this.globalDomains = globalDomains;
		this.projectAbbreviation = TrustDeckHttpClient.require(projectAbbreviation, "projectAbbreviation");
	}

	/**
	 * Lists concise project-domain summaries without fetching full domains.
	 * 
	 * @return project-domain summaries
	 */
	public List<ProjectDomain> getAll() {
		return http.exchange(HttpMethod.GET, http.uri(projectPath("domains"), Map.of()), null,
				new ParameterizedTypeReference<List<ProjectDomain>>() { }, Set.of(200), true).getBody();
	}

	/**
	 * Retrieves a full domain by name.
	 * 
	 * @param domainName domain name
	 * @return domain
	 */
	public Domain get(String domainName) {
		return request(HttpMethod.GET, domainPath(domainName), Map.of(), null, Domain.class, Set.of(200));
	}

	/**
	 * Creates a reduced domain request with project context.
	 * 
	 * @param domain domain definition
	 * @return created domain
	 */
	public Domain create(Domain domain) {
		return request(HttpMethod.POST, new String[] {"api", "domains"}, Map.of(), contextualCopy(domain), 
				Domain.class, Set.of(200, 201));
	}

	/**
	 * Creates a complete domain request with project context.
	 * 
	 * @param domain domain definition
	 * @return created domain
	 */
	public Domain createComplete(Domain domain) {
		return request(HttpMethod.POST, new String[] {"api", "domains", "complete"}, Map.of(), contextualCopy(domain), 
				Domain.class, Set.of(200, 201));
	}

	/**
	 * Retrieves one domain attribute.
	 * 
	 * @param domainName domain name
	 * @param attributeName attribute name
	 * @return attribute value
	 */
	public String getAttribute(String domainName, String attributeName) {
		return request(HttpMethod.GET, domainPath(domainName, attributeName), Map.of(), null, String.class, Set.of(200));
	}

	/**
	 * Retrieves a nested domain subtree.
	 * 
	 * @param domainName root domain name
	 * @return domain tree
	 */
	public DomainTree getSubtree(String domainName) {
		return request(HttpMethod.GET, domainPath(domainName, "subtree"), Map.of(), null, DomainTree.class, Set.of(200));
	}

	/**
	 * Flattens a retrieved subtree depth-first.
	 * 
	 * @param domainName root domain name
	 * @return depth-first domain list
	 */
	public List<Domain> flattenSubtree(String domainName) {
		List<Domain> result = new ArrayList<>();
		flatten(getSubtree(domainName), result);
		
		return result;
	}

	/**
	 * Updates a complete domain request with project context.
	 * 
	 * @param domainName domain name
	 * @param domain replacement definition
	 * @param recursive whether child domains are updated
	 * @return updated domain
	 */
	public Domain updateComplete(String domainName, Domain domain, boolean recursive) {
		return request(HttpMethod.PUT, new String[] {"api", "domains", "complete"},
				Map.of("name", TrustDeckHttpClient.require(domainName, "domainName"), "recursive", recursive), contextualCopy(domain), 
				Domain.class, Set.of(200));
	}

	/**
	 * Updates a reduced domain request with project context.
	 * 
	 * @param domainName domain name
	 * @param domain replacement definition
	 * @return updated domain
	 */
	public Domain update(String domainName, Domain domain) {
		return request(HttpMethod.PUT, new String[] {"api", "domains"}, 
				Map.of("name", TrustDeckHttpClient.require(domainName, "domainName")), contextualCopy(domain), Domain.class, Set.of(200));
	}

	/**
	 * Deletes a domain and optionally its children.
	 * 
	 * @param domainName domain name
	 * @param recursive whether child domains are deleted
	 * @return {@code true} when accepted
	 */
	public boolean delete(String domainName, Boolean recursive) {
		http.empty(HttpMethod.DELETE, http.uri(new String[] {"api", "domains"},
				Map.of("name", TrustDeckHttpClient.require(domainName, "domainName"), "recursive", recursive)), null, Set.of(204), true);
		
		return true;
	}

	/**
	 * Replaces a domain salt.
	 * 
	 * @param domainName domain name
	 * @param salt salt value
	 * @param allowEmpty whether empty salts are accepted
	 * @return updated domain
	 */
	public Domain updateSalt(String domainName, String salt, boolean allowEmpty) {
		return request(HttpMethod.PUT, domainPath(domainName, "salt"),
				Map.of("salt", salt == null ? "" : salt, "allowEmpty", allowEmpty), null, Domain.class, Set.of(200));
	}

	/**
	 * Delegates to global, deliberately unfiltered domain search.
	 * 
	 * @param query search query
	 * @return global search result
	 */
	public SearchResult<Domain> search(String query) {
		return globalDomains.search(query);
	}

	/**
	 * Creates a copy of a domain containing the selected project context.
	 * If the source domain has no project abbreviation, the selected project's
	 * abbreviation is assigned to the copy. The source domain and its algorithm
	 * are not modified.
	 *
	 * @param source domain to copy, or {@code null}
	 * @return the contextualized copy, or {@code null} if the source is {@code null}
	 * @throws IllegalArgumentException if the supplied project abbreviation is blank or does not match the selected project
	 */
	private Domain contextualCopy(Domain source) {
		if (source == null) {
			return null;
		}
		
		String supplied = source.getProjectAbbreviation();
		if (supplied != null && (supplied.isBlank() || !supplied.equalsIgnoreCase(projectAbbreviation))) {
			throw new IllegalArgumentException("Domain projectAbbreviation must match the selected project.");
		}
		
		Domain copy = new Domain();
		copy.setId(source.getId()); copy.setName(source.getName()); copy.setPrefix(source.getPrefix());
		copy.setValidFrom(source.getValidFrom()); copy.setValidFromInherited(source.getValidFromInherited());
		copy.setValidTo(source.getValidTo()); copy.setValidToInherited(source.getValidToInherited());
		copy.setValidityTime(source.getValidityTime()); copy.setEnforceStartDateValidity(source.getEnforceStartDateValidity());
		copy.setEnforceStartDateValidityInherited(source.getEnforceStartDateValidityInherited());
		copy.setEnforceEndDateValidity(source.getEnforceEndDateValidity());
		copy.setEnforceEndDateValidityInherited(source.getEnforceEndDateValidityInherited());
		copy.setAlgorithm(copyAlgorithm(source.getAlgorithm())); copy.setAlgorithmInherited(source.getAlgorithmInherited());
		copy.setMultiplePsnAllowed(source.getMultiplePsnAllowed()); copy.setMultiplePsnAllowedInherited(source.getMultiplePsnAllowedInherited());
		copy.setDescription(source.getDescription()); copy.setSuperDomainID(source.getSuperDomainID());
		copy.setSuperDomainName(source.getSuperDomainName()); copy.setProjectAbbreviation(supplied == null ? projectAbbreviation : supplied);
		
		return copy;
	}

	/**
	 * Creates a copy of an algorithm to prevent modification through the copied domain.
	 *
	 * @param source algorithm to copy, or {@code null}
	 * @return the algorithm copy, or {@code null} if the source is {@code null}
	 */
	private static Algorithm copyAlgorithm(Algorithm source) {
		if (source == null) {
			return null;
		}
		
		return Algorithm.builder().id(source.getId()).name(source.getName()).alphabet(source.getAlphabet())
				.randomAlgorithmDesiredSize(source.getRandomAlgorithmDesiredSize())
				.randomAlgorithmDesiredSuccessProbability(source.getRandomAlgorithmDesiredSuccessProbability())
				.consecutiveValueCounter(source.getConsecutiveValueCounter()).pseudonymLength(source.getPseudonymLength())
				.paddingCharacter(source.getPaddingCharacter()).addCheckDigit(source.getAddCheckDigit())
				.lengthIncludesCheckDigit(source.getLengthIncludesCheckDigit()).salt(source.getSalt())
				.saltLength(source.getSaltLength()).build();
	}

	/**
	 * Adds the domain represented by a hierarchy node and all its descendants to
	 * a flat result list.
	 *
	 * @param tree hierarchy node to flatten, or {@code null}
	 * @param result list receiving the domains
	 */
	private void flatten(DomainTree tree, List<Domain> result) {
		if (tree == null) {
			return;
		}
		
		if (tree.getDomain() != null) {
			result.add(tree.getDomain());
		}
		
		if (tree.getChildren() != null) {
			tree.getChildren().forEach(child -> flatten(child, result));
		}
	}

	/**
	 * Executes an authenticated domain request and returns its response body.
	 *
	 * @param <T> response body type
	 * @param method HTTP method to use
	 * @param path endpoint path segments
	 * @param query query parameters to include
	 * @param body request body, or {@code null} if none is required
	 * @param type expected response body type
	 * @param statuses accepted HTTP response status codes
	 * @return the deserialized response body, or {@code null} if none was returned
	 */
	private <T> T request(HttpMethod method, String[] path, Map<String, ?> query, Object body, Class<T> type, Set<Integer> statuses) {
		return http.exchange(method, http.uri(path, query), body, type, statuses, true).getBody();
	}

	/**
	 * Builds a domain endpoint path and validates the domain-name segment.
	 *
	 * @param domainName name of the domain
	 * @param suffix additional path segments
	 * @return the complete domain endpoint path segments
	 */
	private String[] domainPath(String domainName, String... suffix) {
		String[] path = new String[3 + suffix.length];
		path[0] = "api";
		path[1] = "domains";
		path[2] = TrustDeckHttpClient.require(domainName, "domainName");
		
		System.arraycopy(suffix, 0, path, 3, suffix.length);
		
		return path;
	}

	/**
	 * Builds an endpoint path beneath the selected project.
	 *
	 * @param suffix project endpoint path segment
	 * @return the complete project endpoint path segments
	 */
	private String[] projectPath(String suffix) {
		return new String[] {"api", "projects", projectAbbreviation, suffix};
	}
}
