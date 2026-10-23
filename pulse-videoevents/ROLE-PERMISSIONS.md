# Role permissions

JPA users are stored in security_users/security_user_roles (V7). These are new backend tables, not a migration of the older auth.* project SQL model. Permissions are defined by AppRole in code, roles are assigned in DB. There are no direct per-user grants and no public registration.

OPERATOR: read all incidents, attach evidence. REVIEWER: read, assign, confirm, reject. RESPONSIBLE: read/attach evidence/close only assigned incidents. AUDITOR: read incidents plus audit:read authority (audit endpoint is not implemented). INTEGRATION: ingest video events only. ADMIN: all listed permissions and user management. Multiple roles combine their authorities. Existing lifecycle rules remain enforced; a permission does not make an invalid status transition valid.

The two old in-memory accounts are removed. Initialize the first admin explicitly using environment variables APP_SECURITY_BOOTSTRAP_ENABLED=true, APP_SECURITY_BOOTSTRAP_USERNAME=<name>, APP_SECURITY_BOOTSTRAP_PASSWORD=<strong password>. Bootstrap requires 16+ characters and at most 72 UTF-8 bytes; disable it and remove the password afterwards. BCrypt hashes are stored, never plaintext. No secrets are seeded by Flyway.

POST /api/admin/users creates a user and roles. PUT /api/admin/users/{id}/roles changes roles; self-role changes are forbidden. These operations require user:manage. Assignment assigneeId must be the UUID of a security user; referential validation of existing incident assignments is not added in this change. There is no user listing/password reset/disable endpoint yet.

Existing JWT filter reloads UserDetails on each request, so permissions come from DB rather than trusting role claims. Disabled/nonexistent accounts cannot be loaded. JWT roles claim naming is unchanged and may include permission authorities; it is not the enforcement source.

Incident HTTP mutations hold a pessimistic lock while checking assignment and calling the transactional service. Checks currently live at the HTTP boundary, not all internal service entry points. Reads filter response lists in memory; database pagination/scoped query optimization is pending. Missing ID returns 404 before scope validation on GET, so existence concealment is not guaranteed.

Prometheus remains public for compatibility with existing scraping; production monitoring security is pending. The existing H2/Flyway profile and old tests expecting demo accounts need adaptation. New unit tests are supplied but Maven/Docker/security integration tests have NOT been run. No passing build or full stabilization is claimed.
