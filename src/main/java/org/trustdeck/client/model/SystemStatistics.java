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

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Typed statistics describing the TrustDeck application, database, connection
 * pool, and JVM memory.
 *
 * @author Armin Müller
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemStatistics {

    /** Creates an empty statistics model for JSON binding. */
    public SystemStatistics() { }

    /** Running application build information. */
    private ApplicationInfo application;

    /** PostgreSQL database and table information. */
    private DatabaseInfo database;

    /** TrustDeck Hikari connection-pool information. */
    private ConnectionPoolInfo connectionPool;

    /** JVM memory information, not host or container memory. */
    private JvmMemoryInfo jvmMemory;

    /** Application version and build metadata. */
    @Data
    @AllArgsConstructor
    public static class ApplicationInfo {

        /** Creates empty application information for JSON binding. */
        public ApplicationInfo() { }

        /** Application version. */
        private String version;

        /** Full Git commit identifier used to build the application. */
        private String buildCommit;
    }

    /** Database time, size, and table information. */
    @Data
    @AllArgsConstructor
    public static class DatabaseInfo {

        /** Creates empty database information for JSON binding. */
        public DatabaseInfo() { }

        /** Current time reported by PostgreSQL. */
        private OffsetDateTime databaseTime;

        /** Database size in bytes. */
        private long sizeBytes;

        /** Database table statistics. */
        private List<TableInfo> tables;
    }

    /** Record count and storage information for one database table. */
    @Data
    @AllArgsConstructor
    public static class TableInfo {

        /** Creates an empty table record for JSON binding. */
        public TableInfo() { }

        /** Database schema name. */
        private String schemaName;

        /** Database table name. */
        private String tableName;

        /** Table record count. */
        private long recordCount;

        /** Whether the record count is exact. */
        private boolean recordCountExact;

        /** Table size in bytes. */
        private long tableSizeBytes;

        /** Index size in bytes. */
        private long indexSizeBytes;

        /** TOAST size in bytes. */
        private long toastSizeBytes;

        /** Total relation size in bytes. */
        private long totalRelationSizeBytes;
    }

    /** TrustDeck Hikari connection-pool values. */
    @Data
    @AllArgsConstructor
    public static class ConnectionPoolInfo {

        /** Creates empty connection-pool information for JSON binding. */
        public ConnectionPoolInfo() { }

        /** Active connection count. */
        private int activeConnections;

        /** Idle connection count. */
        private int idleConnections;

        /** Total connection count. */
        private int totalConnections;

        /** Threads awaiting a connection. */
        private int threadsAwaitingConnection;

        /** Maximum pool size. */
        private int maximumPoolSize;

        /** Minimum idle connections. */
        private int minimumIdleConnections;
    }

    /** JVM heap and non-heap memory values, all in bytes. */
    @Data
    @AllArgsConstructor
    public static class JvmMemoryInfo {

        /** Creates empty JVM memory information for JSON binding. */
        public JvmMemoryInfo() { }

        /** Used JVM heap memory in bytes. */
        private long heapUsedBytes;

        /** Committed JVM heap memory in bytes. */
        private long heapCommittedBytes;

        /** Maximum JVM heap memory in bytes. */
        private long heapMaximumBytes;

        /** Used JVM non-heap memory in bytes. */
        private long nonHeapUsedBytes;

        /** Committed JVM non-heap memory in bytes. */
        private long nonHeapCommittedBytes;
    }
}
