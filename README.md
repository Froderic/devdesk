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
When thinking about the most often used CRUD operations of DevDesk in a typical usage scenario, read operations of tickets and projects seemed to be the one that easily came to mind. So a Redis caching of tickets and projects, evicted in case of any changes, expiring in 10 mins was implemented to help reduce database calls.

For cached project stats (how many tickets, open tickets, closed tickets, and etc.) invalidation type, all entries were invalidated instead of per project key invalidation. This seemed fair as projects stats is more of a nice-to-have information that’s not looked up that often and implementing the extra logic to avoid invalidating other project stats (which will be invalidated upon non-get interaction anyways) is a marginal benefit compared to the added complexity.

### Why Optimistic Locking?
In DevDesk usage scenarios, collaboration (therefore simultaneous ticket updates) also could not be ignored. To avoid disastrous silent overwrites of ticket updates, optimistic locking that functions via `@Version` field of ticket entity was implemented. Hibernate checks for version on ticket update calls and throws 409 Conflict, `ObjectOptimisticLockingFailureException`, on mismatch; much better than scrambling to figure out overwritten parameters upon accidental overwrites (if it’s discovered at all).

Pessimistic locking was another option considered but was ruled out as truly simultaneous edits are rare and additional overhead & cautiousness (complete lock of ticket, queueing up of same ticket operations, deadlocks, and etc.) was not deemed favorable.

### Why JWT over Sessions?
JWT is a stateless authentication method meaning it skips the need for a server-side storage and handling scaling of number of users straightforward. Simple HS256 token validation in any instance, expiring after 24 hours. 

Security could have been further heightened by either implementing a blocklist (which reintroduces state) and/or paired short length access and refresh tokens but for the current level of the project, the simple JWT implementation was deemed adequate. 

### Exception Hierarchy and HTTP Semantics
To provide useful HTTP error codes to the user and ease debugging, each of the error conditions were assigned its own exception class (`ResourceNotFoundException`, `InvalidStatusTransitionException`, `DuplicateEmailException`, `OptimisticLockException`) and caught with `GlobalExceptionHandler`. The implementation of a separate `GlobalExceptionHandler` also kept error handling out of service and controller logic, keeping it focused and concise. 

### Status Workflow Enforcement
Rather than having a chaotic, freely-set-able ticket workflow statuses that can go from closed to in-progress, valid ticket transitions were implemented directly in the `TicketStatus` enum with a valid transition hashset and `canTransitionTo()` method that keeps status transitions ordered, logical, and well-contained within the enum itself. 

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
