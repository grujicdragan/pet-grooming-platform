# Pet Grooming Platform

Monorepo for the multi-tenant pet grooming platform (first tenant: **Šapelier** / `sapelier`).

```
pet-grooming-platform/
├── web/        Angular frontend
├── api/        Spring Boot API
└── database/   PostgreSQL schema + seed
```

## Prerequisites

- Node.js 22+
- Java 25 LTS + Maven (or Docker)
- Docker (recommended for Postgres)

## Database

```bash
cd database
docker compose up -d
```

Postgres: `postgresql://petgrooming:petgrooming@localhost:5432/petgrooming`

Schema + Sapelier seed are applied by Flyway when the API starts.

## API

```bash
cd api
# with Java/Maven locally:
mvn spring-boot:run

# or from repo root with Docker:
docker compose up --build
```

Public endpoint:

`GET http://localhost:8080/api/public/tenants/sapelier`

## Web

```bash
cd web
npm start
```

Requires **Node.js 24+** (or 22.22+) and **Angular 22**.

Dev server: `http://localhost:4200` (proxies `/api` → `http://localhost:8080`)

Auth (sign-in / sign-up) is deferred to the next phase; demo login still works in-memory for booking.
