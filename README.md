# api-gateway

The single entry point of **Assay**. It does five jobs:

- routes the public API to the four services
- checks the token
- limits each client IP
- answers CORS for the web app
- serves the aggregated API docs

The architecture is described in
[fyp-grading-docker-compose/docs/architecture.md](https://github.com/FirasKhalife/fyp-grading-docker-compose/blob/master/docs/architecture.md).

## Routes

Routes are configuration, not code (`assay.gateway` in `application.yml`):

| Paths | Service |
|---|---|
| `/api/demo/**`, `/api/me/**`, `/api/assessments/**`, `/api/teams/**`, `/api/reviewers/**`, `/api/schedule`, `/api/overview/**` | admin-service |
| `/api/evaluations/**`, `/api/my-evaluations/**` | evaluation-service |
| `/api/rubrics/**` | rubric-service |
| `/api/notifications/**`, `/api/activity/**` | notification-service |
| `/api/notifications/stream` | notification-service, as a long-lived SSE stream: no circuit breaker, retry or timeout |
| `/v3/api-docs/{service}` | each service's OpenAPI document, shown at `/swagger-ui.html` |

Every route gets the same filters:

- a circuit breaker with a 503 problem fallback
- a retry for GETs
- a Redis rate limit keyed on `CF-Connecting-IP`, then `X-Forwarded-For`, then the socket address

The limits are 20 requests per second with a burst of 60, and creating or resetting a demo copy is limited
separately to 1 per second with a burst of 5.

**Not routed:** `/internal/**`, every actuator endpoint, and the config server. Anything that isn't listed is
denied.

## Security

- Every `/api` call needs a valid Keycloak token, except `GET /api/demo/personas`. The services check roles again.
- CORS allows the web app's origin (`ALLOWED_ORIGINS`) with `Authorization`, `X-Demo-Session` and `Last-Event-ID`.

## Run and test

```bash
./mvnw spring-boot:run   # needs Redis, Keycloak and Eureka; normally started from fyp-grading-docker-compose
./mvnw test
```

The tests cover what is reachable and what isn't: 401 without a token, and `/internal`, actuator and config
paths closed. They also check that the routes come from configuration, test CORS for the web app, and test how the
client IP is chosen.

Built on Spring Boot 4.0 and Spring Cloud Gateway (WebFlux) 2025.1, Java 25, with a layered, non-root image.

## Lint and CI

- **Formatting:** Spotless with palantir-java-format. `./mvnw spotless:apply` fixes it.
- **Lint:** Checkstyle (`checkstyle.xml`) catches likely bugs and sloppy code.
- The build checks both in the `validate` phase, so `./mvnw verify` runs the lint, the build and every test.
- **GitHub Actions** (`.github/workflows/ci.yml`) runs on every pull request and on pushes to `master`:
  - the lint
  - `verify`, including the Testcontainers tests
  - a build of the Docker image
