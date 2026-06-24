# Journal App — Microservices

A journaling application split into three Spring Boot microservices that communicate
synchronously over HTTP (clients → services) and asynchronously over **Apache Kafka**
(service → service). The services share one **external MongoDB (Atlas)** database — separate
collections per service — and a shared **JWT signing secret**, so tokens issued by `user-service`
are trusted by `journal-service`.

> The original monolith is preserved in [`JournalEntry/`](JournalEntry/) for reference.

---

## Architecture

```
                                  ┌──────────────────────────────────────┐
                                  │              Apache Kafka              │
                                  │   topics: user-events, journal-events  │
                                  └───────▲───────────────────┬───────────┘
                  USER_REGISTERED         │                   │  JOURNAL_CREATED
                  (user-events)           │                   │  (journal-events)
                                          │                   │
   ┌────────────┐  POST /auth/*   ┌───────┴────────┐  /journal/*   ┌──────────────────────┐
   │            │ ───────────────▶│  user-service  │   (JWT)       │   journal-service     │
   │   Client   │   returns JWT   │     :8081      │◀───────────── │        :8082          │
   │  (curl/UI) │ ───────────────────────────────────────────────▶│  validates JWT,       │
   │            │   Bearer token + /journal/*                      │  CRUD journal entries │
   └────────────┘                 └───────┬────────┘               └───────────┬──────────┘
                                          │ writes                             │ writes
                                          ▼                                    ▼
                                  ┌───────────────┐    ┌──────────────────────────────────┐
                                  │ notification- │    │      MongoDB Atlas (external)      │
                                  │   service     │    │   db: journaldb                   │
                                  │    :8083      │    │   collections: users,             │
                                  │ consumes      │    │                journal_entries    │
                                  │ journal-events│    └──────────────────────────────────┘
                                  │ → logs it     │
                                  └───────────────┘
```

| Service | Port | Responsibilities | Kafka |
|---|---|---|---|
| **user-service** | 8081 | `/auth/register`, `/auth/login`; issues JWTs; owns the `users` collection | **produces** `USER_REGISTERED` → `user-events` |
| **journal-service** | 8082 | Journal CRUD; validates JWTs (shared secret); owns `journal_entries` | **produces** `JOURNAL_CREATED` → `journal-events` |
| **notification-service** | 8083 | Consumes journal events and logs them (simulated notification); no business REST API | **consumes** `journal-events` |

**Tech:** Java 22 · Spring Boot 3.3.3 · Spring Security + jjwt 0.12 · Spring Kafka · MongoDB Atlas · Docker Compose.

---

## Prerequisites

- **Docker Desktop** (with Compose v2). Java/Maven aren't needed locally — the build runs inside the images.
- A **MongoDB Atlas** cluster (or any reachable MongoDB) and its connection string.
- In Atlas → **Network Access**, allowlist the public IP of the machine running Docker
  (or `0.0.0.0/0` for dev only). Containers reach Atlas via your host's IP; if it isn't
  allowlisted, connections time out even with a correct URI.

---

## Configure (`.env`)

Copy the template and fill in real values:

```bash
cp .env.example .env
```

`.env` (read automatically by Compose, gitignored — never commit it):

```env
# Shared HS256 secret for user-service + journal-service (>= 32 bytes). Both MUST use the same value.
JWT_SECRET=<a long random secret>
JWT_EXPIRATION=3600000

# MongoDB Atlas connection string. Include the database name "/journaldb" BEFORE the "?",
# and URL-encode any special characters (@ : / ? # &) in the password.
MONGODB_URI=mongodb+srv://USER:PASSWORD@your-cluster.mongodb.net/journaldb?retryWrites=true&w=majority
```

`MONGODB_URI` and `JWT_SECRET` are **required** — if either is missing, `docker compose up`
fails immediately with a clear message (no silent dev fallback). `application.yml` contains no
default connection/secret values; all config is injected from the environment.

---

## Run it (single command)

```bash
# from the repo root (the folder containing docker-compose.yml)
docker compose up --build
```

This starts Zookeeper → Kafka, then builds and starts the three services (which connect to your
Atlas DB). First run takes a few minutes (Maven downloads dependencies inside the build stage).

Useful variants:

```bash
docker compose up --build -d                  # detached
docker compose logs -f notification-service   # watch the notification log
docker compose ps                             # status
docker compose down                           # stop the stack (your data stays in Atlas)
```

Once up:
- user-service     → http://localhost:8081
- journal-service  → http://localhost:8082
- notification     → http://localhost:8083/actuator/health
- Kafka (host)     → localhost:9092

---

## API endpoints & example curl requests

### user-service (`:8081`)

**Register** — creates a USER and emits `USER_REGISTERED`:
```bash
curl -i -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{"userName":"ram","password":"secret123","email":"ram@example.com"}'
# 201 Created   (409 if the username is taken, 400 if userName/password missing)
```

**Login** — returns a JWT:
```bash
curl -s -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"ram","password":"secret123"}'
# {"token":"eyJhbGciOi...", "userName":"ram", "roles":["USER"]}   (401 if invalid)
```

Grab the token into a shell variable for the journal calls:
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userName":"ram","password":"secret123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
echo "$TOKEN"
```

### journal-service (`:8082`) — all require `Authorization: Bearer <token>`

**Create an entry** — body is just `{title, content}` (owner comes from the JWT, date is set
server-side). Emits `JOURNAL_CREATED`:
```bash
curl -s -X POST http://localhost:8082/journal \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"My first day","content":"Today I split a monolith."}'
# 201 Created. Example response (note id is a plain hex string):
# {"id":"6a3a768df170cf2aecd5a940","userName":"ram","title":"My first day",
#  "content":"Today I split a monolith.","date":"2026-06-23T12:05:34.56"}
```

**List my entries** (`404` if I have none):
```bash
curl -s http://localhost:8082/journal -H "Authorization: Bearer $TOKEN"
```

Capture the id of the most recent entry for the by-id calls below:
```bash
ID=$(curl -s http://localhost:8082/journal -H "Authorization: Bearer $TOKEN" \
  | sed -E 's/.*"id":"([a-f0-9]{24})".*/\1/')
echo "$ID"
```

**Get one by id** (`404` if not yours / not found):
```bash
curl -s http://localhost:8082/journal/id/$ID -H "Authorization: Bearer $TOKEN"
```

**Update** — `{title, content}`; blank fields are ignored:
```bash
curl -i -X PUT http://localhost:8082/journal/id/$ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated title","content":"Edited body."}'
```

**Delete:**
```bash
curl -i -X DELETE http://localhost:8082/journal/id/$ID -H "Authorization: Bearer $TOKEN"
# 204 No Content   (404 if not found)
```

**Admin — read everyone's entries** (requires a token whose roles include `ADMIN`):
```bash
curl -s http://localhost:8082/journal/all -H "Authorization: Bearer $ADMIN_TOKEN"
```

**Auth behavior:** missing/expired/invalid token → `401`; valid token but insufficient role
(e.g. a USER calling `/journal/all`) → `403`.

> Registration always creates a `USER`. To get an ADMIN, set that user's `roles` array to include
> `"ADMIN"` in the `users` collection (e.g. via Atlas UI or `mongosh`) and log in again.

### notification-service (`:8083`)

No business endpoints. It reacts to Kafka events and logs them. Health only:
```bash
curl -s http://localhost:8083/actuator/health
```

---

## How the Kafka event flow works

Services never call each other directly for these events — they publish/subscribe through Kafka,
so producers don't block on (or even know about) consumers.

**1. Registration flow (`user-events`)**
```
client → POST /auth/register → user-service saves user
                              → user-service publishes USER_REGISTERED to "user-events"
```
The `user-events` topic exists and is produced to; no consumer is wired yet (it's there to
demonstrate the pattern and as a hook for future features like a welcome email).

**2. Journal creation flow (`journal-events`)**
```
client → POST /journal (Bearer JWT) → journal-service saves entry
                                     → publishes JOURNAL_CREATED to "journal-events"
                                                          │
                                       notification-service @KafkaListener consumes it
                                                          │
                                                  logs a "notification"
```

To see it live:
```bash
docker compose logs -f notification-service
# then create a journal entry; you'll see a NOTIFICATION block appear
```

**Serialization:** events are sent as plain JSON with Spring Kafka's `JsonSerializer`
(`spring.json.add.type.headers=false`). The consumer is configured with a default target type
(`JsonDeserializer` + `spring.json.value.default.type`), so the producer and consumer can keep
their own copies of the event class in different packages without coupling.

**Topics** (auto-created on startup; partitions=1, replicas=1 for local dev):
- `user-events` — produced by user-service
- `journal-events` — produced by journal-service, consumed by notification-service

---

## Repository layout

```
.
├── docker-compose.yml         # zookeeper, kafka, + 3 services (MongoDB is external/Atlas)
├── .env.example               # template: MONGODB_URI, JWT secret / token TTL
├── README.md
├── user-service/              # :8081  pom.xml + Dockerfile + src
├── journal-service/           # :8082  pom.xml + Dockerfile + src
├── notification-service/      # :8083  pom.xml + Dockerfile + src
└── JournalEntry/              # original monolith (kept for reference)
```

Each service is an independent Maven project (its own `pom.xml`) and builds to its own image —
no parent/multi-module POM, by design.

---

## Notes & trade-offs

- **External MongoDB (Atlas):** one database (`journaldb`), separate collections per service
  (`users`, `journal_entries`). A stricter microservice setup would give each service its own
  database/instance; sharing one keeps this project simple.
- **Shared secret JWT validation** keeps things simple (HS256, symmetric). For production prefer
  asymmetric signing (RS256): user-service signs with a private key, others verify with the public
  key — no shared secret to leak.
- **Strict config:** `application.yml` carries no default secrets/URIs; `MONGODB_URI` and
  `JWT_SECRET` must come from `.env`/the environment, so a misconfigured deploy fails fast.
- **Event classes are duplicated** in producer and consumer (no shared module) to honor the
  "each service standalone" constraint; keep their JSON shapes in sync.
- Dropped from the monolith as out of scope for this split: weather, Redis cache, email/scheduler,
  and sentiment analysis. The journal `sentiment` field was also dropped (it belonged to that feature).
```
