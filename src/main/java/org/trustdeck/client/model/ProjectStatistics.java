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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Typed statistics for one TrustDeck project.
 *
 * @author Armin Müller
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectStatistics {

    /** Creates an empty statistics model for JSON binding. */
    public ProjectStatistics() { }

    /** UTC instant when the statistics were generated. */
    private Instant generatedAt;

    /** Project identity and validity information. */
    private ProjectDetails project;

    /** Exact aggregate project counts. */
    private Counts counts;

    /** Entities grouped by entity-type name. */
    private List<NamedCount> entitiesByType;

    /** Pseudonyms grouped by project-owned domain name. */
    private List<NamedCount> pseudonymsByDomain;

    /** Project identity and validity information. */
    @Data
    @AllArgsConstructor
    public static class ProjectDetails {

        /** Creates empty project details for JSON binding. */
        public ProjectDetails() { }

        /** Project name. */
        private String name;

        /** Project abbreviation. */
        private String abbreviation;

        /** Project validity start date and time. */
        private OffsetDateTime startDate;

        /** Project validity end date and time. */
        private OffsetDateTime endDate;

        /** Remaining validity in seconds, null without an end date and zero when expired. */
        private Long remainingValiditySeconds;
    }

    /** Exact aggregate counts for the project. */
    @Data
    @AllArgsConstructor
    public static class Counts {

        /** Creates empty counts for JSON binding. */
        public Counts() { }

        /** Exact number of project-owned domains. */
        private long domains;

        /** Exact number of project entity types. */
        private long entityTypes;

        /** Exact number of project entities. */
        private long entities;

        /** Exact number of project pseudonyms. */
        private long pseudonyms;
    }

    /** A named resource and its exact count. */
    @Data
    @AllArgsConstructor
    public static class NamedCount {

        /** Creates an empty named count for JSON binding. */
        public NamedCount() { }

        /** Name of the counted resource. */
        private String name;

        /** Exact resource count. */
        private long count;
    }
}
