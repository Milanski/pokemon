# Pokémon Collection

> Trainers register, log in, browse Pokémon (data from [PokéAPI](https://pokeapi.co/)), and collect their favorites. Each trainer only ever sees their own collection.

| Layer      | Choice                                                              |
|------------|----------------------------------------------------------------------|
| Backend    | Java 21 · Spring Boot 3 · Maven — hexagonal architecture (ports & adapters) + DDD |
| Frontend   | React 19 · TypeScript · Vite                                        |
| Database   | PostgreSQL, schema-versioned with Flyway                            |
| Auth       | Stateless JWT                                                       |

---

### 1 · Run it

```bash
docker compose up --build
```

→ `localhost:8081` frontend · `localhost:8080/api` backend · `localhost:5434` postgres

`docker compose down` to stop (`-v` also drops the database volume).

### 2 · Run it apart, for development

Backend needs Postgres reachable at `DB_URL` (`docker compose up db` is enough)
and a `JWT_SECRET` — there's no built-in default, so the app fails fast at
startup instead of silently signing tokens with a well-known key:

```bash
cd backend
export JWT_SECRET=$(openssl rand -base64 32)
./mvnw spring-boot:run
```

Frontend proxies `/api` to `localhost:8080` in dev:

```bash
cd frontend
npm install && npm run dev
```

### 3 · Prove it works

```bash
cd backend && ./mvnw test    # domain unit tests, MockMvc slices, one full
                              # Spring Boot integration test, ArchUnit boundary checks
cd frontend && npm test      # Vitest + React Testing Library
```

---

## Under the hood

Three bounded contexts (`backend/.../com/mbls/challenge/pokemon/`), each
layered `domain → application → adapter`, dependencies pointing inward only —
`ArchitectureTest` (ArchUnit) enforces this at build time, not just in code review.

```
trainer     identity & auth — register, login, password hashing, JWT
catalog     anti-corruption layer around PokéAPI
collection  core domain — a trainer's personal Pokémon collection
shared      TrainerId / PokemonId / PageResult + cross-cutting concerns
```

`collection` never imports `catalog`'s domain model directly — it reaches it
through its own outbound port, bridged by a thin adapter
(`CatalogPokemonLookupAdapter`).

<img src="docs/hexagonal-architecture.svg" alt="Hexagonal architecture: a domain/application core with ports, wrapped by an adapter layer split into interface (inbound) and infrastructure (outbound)" width="420">

**Worth knowing:**
- JWT over sessions — keeps the API stateless, fits a SPA.
- List-view sprites are computed from PokéAPI's predictable artwork URL pattern rather than fetched, so browsing costs one API call, not one per Pokémon.
- A trainer's collection is its own aggregate, so "no duplicate Pokémon" is enforced by the domain model, not just a DB constraint.

---

## API

```
POST    /api/auth/register     — create a trainer, returns a JWT        (no auth)
POST    /api/auth/login        — log in, returns a JWT                  (no auth)
GET     /api/pokemon           — paginated list (page, size, search)
GET     /api/pokemon/{id}      — Pokémon details
GET     /api/collection        — the current trainer's collection
POST    /api/collection/{id}   — add a Pokémon to the collection
DELETE  /api/collection/{id}   — remove a Pokémon from the collection
```

JWT goes in `Authorization: Bearer <token>`; logout is client-side. Errors are
RFC 7807 `application/problem+json`. Auth endpoints are rate-limited per IP
(`app.rate-limit.*`, 20 req/60s default). PokéAPI calls carry a Resilience4j
retry + circuit breaker (404s excluded — a missing Pokémon isn't an outage).

---

## Not done yet

No token refresh (re-login after `app.jwt.expiration-minutes`, 2h default) ·
the PokéAPI cache is unbounded in-memory with no TTL, fine for one instance,
not for several (would want Redis) · only per-IP rate limiting, no
per-account lockout, so an attacker spread across IPs could still brute-force
one account · collection writes are optimistic-locked but the frontend
doesn't yet surface the resulting 409 for a retry.
