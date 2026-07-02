# SecureLedger — full-base application layer (Java / Spring Boot)

A deliberately non-trivial **Spring Boot 3 / Java 21** REST service — *SecureLedger*,
a secure task & audit API — packaged on top of the hardened
[`rhel9-hardened-base`](https://github.com/kelleyblackmore/rhel9-hardened-base)
image, and run through the **same DISA STIG + CVE checks the base uses**.

Sibling: [`rhel9-app-micro`](https://github.com/kelleyblackmore/rhel9-app-micro) —
the same service in Rust on the distroless `ubi9-micro` base.

## What the app does

- **JWT auth** (`POST /api/auth/login`) with BCrypt-hashed, seeded users.
- **RBAC** (Spring Security 6): `ROLE_USER` vs `ROLE_ADMIN`.
- **Task CRUD** (`/api/tasks`) with Bean Validation, owner enforcement, paging.
- **Append-only audit log** (`/api/audit`, admin-only) written on every mutation.
- **Rate limiting** (Bucket4j) → HTTP 429 with `Retry-After`.
- **Persistence**: Spring Data JPA + **SQLite**.
- **Observability**: Actuator health (liveness/readiness) + Micrometer
  `/actuator/prometheus`; plus `/healthz` and `/readyz`.
- **OpenAPI**: springdoc → `/swagger-ui.html`, `/v3/api-docs`.

## Image

Multi-stage build: a Maven/Temurin-21 builder compiles and tests the jar; the
runtime stage is `FROM ghcr.io/kelleyblackmore/rhel9-hardened-base:latest`,
`dnf update`s, installs `java-21-openjdk-headless`, and runs as non-root UID 10001.

```bash
docker build -t securedledger-full .
docker run --rm -p 8080:8080 securedledger-full
# http://localhost:8080/swagger-ui.html
```

## CI (all in GitHub Actions)

| Workflow | What it does |
|---|---|
| [`build.yml`](.github/workflows/build.yml) | `mvn verify` — compile + unit/integration tests |
| [`cve-scan.yml`](.github/workflows/cve-scan.yml) | Trivy on the app image; **gate: 0 fixable Critical/High**; unfixed inventoried |
| [`oscap-stig-scan.yml`](.github/workflows/oscap-stig-scan.yml) | OpenSCAP **DISA RHEL 9 STIG** scan of the app image (reuses the base's answer file + attestation), gated |
| [`stig-dast-scan.yml`](.github/workflows/stig-dast-scan.yml) | **DISA API SRG** + **DISA ASD STIG** black-box scans against the running app (`stig-api-scanner` + `stig-asd-scanner`); SARIF → Security tab, CRITICAL-gated |

The DAST configs live in [`stig/api-srg.yaml`](stig/api-srg.yaml) and
[`stig/asd-stig.yaml`](stig/asd-stig.yaml).

## Automation

Weekly **Renovate** ([`renovate.json`](renovate.json)) updates Maven dependencies,
the pinned base-image digest, and GitHub Actions (requires the Mend Renovate App).
