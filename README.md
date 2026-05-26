# DevDesk

A developer issue tracking system (Jira-inspired) built as a capstone backend portfolio project. Designed to demonstrate production-grade engineering decisions, not just implementation.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Database | PostgreSQL 16 |
| Caching | Redis 7 (local) / AWS ElastiCache (production) |
| Auth | Spring Security + JWT (jjwt 0.12.6) |
| Testing | JUnit 5 + Mockito — 92% service layer coverage |
| CI/CD | GitHub Actions |
| Deployment | AWS EC2 + RDS + ElastiCache |
| Documentation | Swagger UI / OpenAPI 3 |
| Build | Maven |

---

## Features

- **User management** with role-based access control (ADMIN, DEVELOPER, REPORTER)
- **Project management** with ownership tracking
- **Ticket lifecycle** with enforced status workflow: `OPEN → IN_PROGRESS → IN_REVIEW → RESOLVED → CLOSED`
- **Audit logging** — every ticket change recorded with who changed what and when
- **Redis caching** for tickets and project stats with cache invalidation
- **Optimistic locking** for concurrent ticket updates
- **Pagination and filtering** on ticket and project endpoints
- **JWT authentication** — stateless, role-based endpoint protection
- **Swagger UI** — interactive API documentation at `/swagger-ui.html`
- **GitHub Actions CI/CD** — automated test pipeline on every push to main

---

## Architecture Decisions

### Why Redis for Caching?
Tickets and project stats are read far more frequently than they are written. Caching at the service layer with a 10-minute TTL reduces database load on the hot read path without sacrificing consistency — cache entries are evicted immediately on any mutation via `@CacheEvict`.

For project stats, `allEntries = true` was chosen over per-project key invalidation. Tracking affected project IDs across ticket mutations adds implementation complexity that isn't justified given stats are not on the critical path.

Production deployment uses AWS ElastiCache; local development uses a Docker-managed Redis instance for environment parity.

### Why Optimistic Locking?
Concurrent ticket updates without concurrency control cause silent lost updates — the second write overwrites the first with no error. Optimistic locking adds a `@Version` field to the Ticket entity; Hibernate validates the version on every UPDATE and throws `ObjectOptimisticLockingFailureException` on mismatch, which the exception handler maps to a clean 409 Conflict response.

Pessimistic locking was ruled out because it blocks concurrent reads and introduces contention that isn't warranted when simultaneous edits are rare. Optimistic locking keeps reads non-blocking and surfaces conflicts only when they actually occur.

### Why JWT over Sessions?
Stateless authentication eliminates server-side session storage, making horizontal scaling straightforward — any instance can validate any token without shared state. Tokens are signed with HS256 and expire after 24 hours.

The known tradeoff is that JWTs cannot be revoked before expiry without reintroducing state via a blocklist. For this use case the tradeoff is acceptable; a production system would pair short-lived access tokens with refresh tokens to reduce the revocation window.

### Exception Hierarchy and HTTP Semantics
Each error condition has a dedicated exception class — `ResourceNotFoundException`, `InvalidStatusTransitionException`, `DuplicateEmailException`, `OptimisticLockException` — handled centrally in `GlobalExceptionHandler`. This maps domain errors directly to HTTP semantics (404, 400, 409, 409) and keeps error handling out of service and controller logic.

### Status Workflow Enforcement
Valid ticket transitions are defined directly on the `TicketStatus` enum via a `canTransitionTo()` method backed by a static transition map. Encoding workflow rules on the domain object itself — rather than in service methods — keeps the constraint co-located with the concept it governs and makes invalid transitions impossible to reach without explicitly bypassing the enum.

---

## API Overview

Full interactive documentation available at `/swagger-ui.html` when running locally.

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/login` | Login, returns JWT | None |
| POST | `/api/users` | Create user | None |
| GET | `/api/users` | List all users | ADMIN |
| DELETE | `/api/users/{id}` | Delete user | ADMIN |
| PATCH | `/api/users/{id}/change-password` | Change password | Auth |
| POST | `/api/projects` | Create project | Auth |
| GET | `/api/projects` | List projects (paginated) | Auth |
| GET | `/api/projects/{id}/stats` | Ticket counts by status | Auth |
| POST | `/api/tickets` | Create ticket | Auth |
| GET | `/api/tickets` | List tickets (paginated, filterable) | Auth |
| GET | `/api/tickets/{id}` | Get ticket (cached) | Auth |
| PATCH | `/api/tickets/{id}` | Update ticket | Auth |
| PATCH | `/api/tickets/{id}/assign` | Assign ticket to user | Auth |
| DELETE | `/api/tickets/{id}` | Delete ticket | ADMIN |
| GET | `/api/tickets/{id}/audit-logs` | Ticket change history | Auth |

**Filtering:** `GET /api/tickets?status=OPEN&projectId=1&page=0&size=10`

---

## Running Locally

### Prerequisites
- Java 21
- Docker Desktop
- Maven

### Steps

```bash
# Clone the repository
git clone https://github.com/Froderic/devdesk.git
cd devdesk

# Start PostgreSQL and Redis
docker compose up -d

# Run the application
mvn spring-boot:run
```

Application starts on `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Testing

```bash
mvn test
```

- **92% service layer line coverage** (JUnit 5 + Mockito)
- Unit tests for all service classes — `UserService`, `ProjectService`, `TicketService`, `AuditLogService`, `AuthService`, `JwtService`
- Tests cover happy paths, not-found exceptions, invalid transitions, duplicate email, optimistic lock conflicts, and expired JWT tokens

---

## CI/CD Pipeline

GitHub Actions runs on every push and pull request to `main`:

1. Spin up PostgreSQL 16 and Redis 7 containers
2. Set up JDK 21 (Temurin)
3. Restore Maven dependency cache
4. Run `mvn test`
5. Report pass/fail

Pipeline configuration: `.github/workflows/ci.yml`

---

## AWS Deployment Architecture

```
Internet
    │
    ▼
EC2 (t3.micro)
Spring Boot App
    │
    ├──► RDS PostgreSQL (db.t3.micro)
    │
    └──► ElastiCache Redis (cache.t3.micro)
```

All three services run in the same VPC for private network communication. Security groups restrict RDS and ElastiCache access to the EC2 instance only.

---

## Known Issues / Future Improvements

- JWT tokens cannot be invalidated before expiry (would require a token blocklist/Redis-based solution)
- CVE-2026-34483 — Apache Tomcat vulnerability in `JsonAccessLogValve`. Mitigation: `JsonAccessLogValve` is not configured in this application. Fix: upgrade to Spring Boot 4.0.6+ when available with Tomcat 11.0.21
- No frontend (intentional scope decision — backend portfolio focus)
- Password change endpoint does not require re-authentication (acceptable for portfolio scope)
