/*
 * Trust Deck Client Library
 * Copyright 2025 TrustDeck Team
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

import org.trustdeck.client.config.TrustDeckClientConfig;
import org.trustdeck.client.service.Domains;
import org.trustdeck.client.service.Persons;
import org.trustdeck.client.service.Pseudonyms;
import org.trustdeck.client.service.TrustDeckTokenService;
import org.trustdeck.client.util.TrustDeckRequestUtil;

import lombok.Getter;

/**
 * This class encapsulates the connector subclasses.
 * 
 * @author Armin Müller
 */
public class TrustDeckClient {
	
	/** A service handling the authentication. */
	@Getter
	private TrustDeckTokenService tokenService;

	/** Enables access to utility functions. */
	@Getter
	private TrustDeckRequestUtil util;
	
	/** Enables access to the config parameters. */
	private TrustDeckClientConfig config;

	/** Connector for the domain-scope. */
	private Domains domains;

	/** Connector for the person-scope. */
	private Persons persons;

	/**
	 * Constructor initializing all needed sub-connectors.
	 * 
	 * @param config the configuration for this client instance
	 */
	public TrustDeckClient(TrustDeckClientConfig config) {
		this.config = config;
		this.tokenService = new TrustDeckTokenService(config);
		this.util = new TrustDeckRequestUtil(tokenService);
		this.domains = new Domains(config, util);
		this.persons = new Persons(config, util);
	}

	/**
	 * Enables access to API methods for the domain-scope.
	 * 
	 * @return the domain connector
	 */
	public Domains domains() {
		return this.domains;
	}

	/**
	 * Enables access to API methods for the pseudonym-scope.
	 * 
	 * @return the pseudonym connector
	 */
	public Pseudonyms pseudonyms(String domainName) {
		return new Pseudonyms(config, util, domainName);
	}

	/**
	 * Enables access to API methods for the person-scope.
	 * 
	 * @return the person connector
	 */
	public Persons persons() {
		return this.persons;
	}
}
