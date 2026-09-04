# Endpoint Coverage

Backend baseline: `54bf747d0bfe04dfc0a816b4e49c2944abd71e60` (`v2.2.0`). The 69 business endpoints below exclude `DatabaseMaintenanceController`, error handling, Swagger/OpenAPI UI, CORS `OPTIONS`, and the Kafka listener.

| Operation | Method | Path | Required mapping condition | Client result |
| --- | --- | --- | --- | --- |
| health | GET | /api/health | HealthStatus |
| domain-create | POST | /api/domains | Domain |
| domain-create-complete | POST | /api/domains/complete | Domain |
| domain-delete | DELETE | /api/domains | boolean |
| domain-attribute | GET | /api/domains/{domain}/{attribute} | String |
| domain-get | GET | /api/domains/{domain} | Domain |
| subtree | GET | /api/domains/{domain}/subtree | DomainTree |
| domain-hierarchy | GET | /api/domains/hierarchy | List<DomainTree> |
| domain-update-complete | PUT | /api/domains/complete | Domain |
| domain-update | PUT | /api/domains | Domain |
| domain-salt | PUT | /api/domains/{domain}/salt | Domain |
| domain-search | GET | /api/domains | SearchResult<Domain> |
| pseudonym-create-batch | POST | /api/domains/{domain}/pseudonyms/batch | BatchResult<Pseudonym> |
| pseudonym-create | POST | /api/domains/{domain}/pseudonyms | Pseudonym |
| pseudonym-delete-batch | DELETE | /api/domains/{domain}/pseudonyms/batch | BatchResult<Boolean> |
| single-delete | DELETE | /api/domains/{domain}/pseudonyms | 204 only boolean |
| linked-pseudonyms | GET | /api/domains/linked-pseudonyms | List<List<Pseudonym>> |
| pseudonym-get-batch | GET | /api/domains/{domain}/pseudonyms/batch | List<Pseudonym> |
| pseudonym-get-identifier | GET | /api/domains/{domain}/pseudonyms | `id`, `idType` | Pseudonym |
| pseudonym-get-psn | GET | /api/domains/{domain}/pseudonyms | `psn` | Pseudonym |
| pseudonym-update-batch | PUT | /api/domains/{domain}/pseudonyms/batch | List<Pseudonym> |
| pseudonym-update-complete | PUT | /api/domains/{domain}/pseudonyms/complete | Pseudonym |
| pseudonym-update | PUT | /api/domains/{domain}/pseudonyms | Pseudonym |
| pseudonym-validation | GET | /api/domains/{domain}/pseudonyms/validation | boolean |
| pseudonym-search | GET | /api/domains/{domain}/pseudonyms | `query` | SearchResult<Pseudonym> |
| project-create | POST | /api/projects | Project |
| project-list | GET | /api/projects | List<Project> |
| project-get | GET | /api/projects/{project} | Project |
| project-domains | GET | /api/projects/{project}/domains | List<ProjectDomain> |
| project-statistics | GET | /api/projects/{project}/statistics | JsonNode/501 |
| project-update | PUT | /api/projects/{project} | Optional<Project> |
| project-delete | DELETE | /api/projects/{project} | boolean |
| image-create | POST | /api/projects/{project}/image | boolean |
| image-get | GET | /api/projects/{project}/image | ProjectImage |
| image-update | PUT | /api/projects/{project}/image | boolean |
| image-delete | DELETE | /api/projects/{project}/image | boolean |
| base-type-create | POST | /api/entities/base-types | EntityType |
| base-type-get | GET | /api/entities/base-types/{type} | EntityType |
| base-type-search | GET | /api/entities/base-types | SearchResult<EntityType> |
| type-create | POST | /api/projects/{project}/entities/config | EntityType |
| type-get | GET | /api/projects/{project}/entities/config/{type} | EntityType |
| type-update | PUT | /api/projects/{project}/entities/config/{type} | EntityType |
| type-delete | DELETE | /api/projects/{project}/entities/config/{type} | boolean |
| type-search | GET | /api/projects/{project}/entities | SearchResult<EntityType> |
| entity-create | POST | /api/projects/{project}/entities/{type} | Entity |
| entity-get | GET | /api/projects/{project}/entities/{type}/{id} | Entity |
| entity-update | PUT | /api/projects/{project}/entities/{type}/{id} | Entity |
| entity-delete | DELETE | /api/projects/{project}/entities/{type}/{id} | boolean |
| entity-search | GET | /api/projects/{project}/entities/{type} | SearchResult<Entity> |
| entity-pseudonyms | GET | /api/projects/{project}/entities/{type}/{id}/pseudonyms | SearchResult<Pseudonym> |
| record-linkage | POST | /api/projects/{project}/entities/{type}/record-linkage | List<RecordLinkageCandidate> |
| user-search | GET | /api/permissions/users | SearchResult<User> |
| permission-domain-create | POST | /api/permissions/domains/{domain} | BatchResult<Permission> |
| permission-project-create | POST | /api/permissions/projects/{project} | BatchResult<Permission> |
| permission-global-create | POST | /api/permissions/global | BatchResult<Permission> |
| permission-type-create | POST | /api/permissions/projects/{project}/entity-types/{type} | BatchResult<Permission> |
| permission-domain-get | GET | /api/permissions/domains/{domain} | List<Permission> |
| permission-project-get | GET | /api/permissions/projects/{project} | List<Permission> |
| permission-global-get | GET | /api/permissions/global | List<Permission> |
| permission-type-get | GET | /api/permissions/projects/{project}/entity-types/{type} | List<Permission> |
| permission-available | GET | /api/permissions | List<EffectivePermission> |
| permission-domain-update | PUT | /api/permissions/domains/{domain} | boolean |
| permission-project-update | PUT | /api/permissions/projects/{project} | boolean |
| permission-global-update | PUT | /api/permissions/global | boolean |
| permission-type-update | PUT | /api/permissions/projects/{project}/entity-types/{type} | boolean |
| permission-domain-delete | DELETE | /api/permissions/domains/{domain} | BatchResult<Boolean> |
| permission-project-delete | DELETE | /api/permissions/projects/{project} | BatchResult<Boolean> |
| permission-global-delete | DELETE | /api/permissions/global | BatchResult<Boolean> |
| permission-type-delete | DELETE | /api/permissions/projects/{project}/entity-types/{type} | BatchResult<Boolean> |

Corrections: subtree remains a `DomainTree`; single pseudonym delete is 204-only; `Pseudonym.domain` is excluded because the backend emits it with `@JsonIgnore`; backend-only `PseudonymUpdate.oldDomain` and `newDomain` are excluded.
