# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Rooflet is a property management platform for landlords. It's a monorepo with a Spring Boot API (`apps/api`), a Next.js frontend (`apps/web`), and a MySQL database. Services are orchestrated via Docker Compose with an Nginx reverse proxy.

## Common Commands

All commands are available via the root `Makefile`:

```bash
make web-dev        # Start frontend dev server (bun)
make web-install    # Install frontend dependencies (bun)
make web-build      # Production build
make web-test       # Run frontend tests (vitest)
make api-dev        # Start API in dev mode (requires MySQL running)
make db-reseed      # Drop, recreate, and restore database from dump
make db-dump        # Export database to sql/rooflet_dump.sql
```

**Frontend-specific (from apps/web):**
```bash
bun run lint        # ESLint
bun run lint:fix    # ESLint with auto-fix
bun run test:watch  # Vitest in watch mode
bun run test:ui     # Vitest with browser UI
```

**Running a single test file:**
```bash
cd apps/web && bunx vitest run path/to/test.test.ts
```

**Docker (full stack):**
```bash
docker-compose up   # API on :8081, Web via Nginx on :3001, MySQL on :3306
```

## Architecture

### Backend (apps/api) — Spring Boot 3 / Java 21

**Layered architecture:** Controller → Service → Repository → Entity

- **Package root:** `io.rooflet` (`src/main/java/io/rooflet/`)
- **config/** — Spring Security configs (profile-based: `DevSecurityConfig`, `ProductionSecurityConfig`), OpenAPI config
- **controller/** — REST endpoints under `/api/*` (properties, tenants, rent-collections, expenses, portfolios, users, market-listings, feedback)
- **service/** — Business logic; `AuthenticationService` manages session auth, `PortfolioPermissionService` enforces role-based access (OWNER/MANAGER/VIEWER)
- **repository/** — Spring Data JPA repositories with method derivation and `@Query`
- **model/entity/** — JPA entities with UUID primary keys (BINARY 16), Lombok annotations, soft deletes via `archived` flag
- **model/request/** and **model/response/** — DTOs for API contracts
- **model/enums/** — `PropertyType`, `ExpenseCategory`, `PortfolioRole`

**Auth:** Session-based (31-day timeout), BCrypt cost factor 12. No JWT. CORS origins configured via `CORS_ALLOWED_ORIGINS` env var.

**Database migrations:** Flyway, located in `src/main/resources/db/migration/` (V1 schema, V2 seed data, V3 market data).

**Spring profiles:** `dev` (enables H2 console, Swagger UI) and `prod` (stricter security).

### Frontend (apps/web) — Next.js 15 / React 19 / TypeScript

**App Router with this layout hierarchy:**
```
RootLayout → ReduxProvider → ThemeProvider → AuthWrapper → LayoutContent (sidebar + page)
```

**Key directories:**
- **app/** — Route pages. Main groups: `/operations` (properties, tenants, rent-collection, expenses), `/business-development` (market-listings, portfolio), `/reports`, `/settings`
- **components/ui/** — shadcn/ui primitives (Radix UI + Tailwind CSS)
- **components/** — Feature components (dashboard widgets, wizard, sidebar, auth-wrapper)
- **store/** — Redux Toolkit store with redux-persist. Persisted slices: `auth`, `portfolio`
- **store/slices/** — `authSlice`, `portfolioSlice`, `propertiesSlice`, `tenantsSlice`, `rentCollectionsSlice`, `expensesSlice`
- **lib/api/** — Typed API client files per resource (properties.ts, tenants.ts, etc.). Types centralized in `lib/api/types.ts`
- **lib/api-client.ts** — Base `apiRequest<T>()` with `credentials: 'include'` for session cookies, dispatches `api-auth-error` DOM events on 401/403

**Auth flow:** `AuthWrapper` checks session on mount via `checkAuth()` thunk (calls `/api/users/me`), redirects unauthenticated users to `/login`, listens for `api-auth-error` events for session expiry.

**State management pattern:** Redux async thunks call typed API client → response updates slice state → components re-render via selectors.

### Multi-tenancy Model

Authorization is portfolio-based. Users belong to portfolios via `PortfolioMember` with roles:
- **OWNER** — Full control including member management
- **MANAGER** — Create/edit resources
- **VIEWER** — Read-only

The active portfolio ID is stored in the Spring session. All resource queries are scoped to the active portfolio.
