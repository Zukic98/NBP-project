# PROJECT KNOWLEDGE BASE

**Generated:** 2026-05-02 **Commit:** b7a8aea **Branch:** docs/issue-27

## OVERVIEW

SUDS (Sistem za Upravljanje Dokazima i Slučajevima) — full-stack evidence &
case-management web app for law-enforcement, built for ETF Sarajevo's *Napredne
baze podataka* course. Spring Boot 4.0.4 REST API + React 19 SPA, Oracle 19c via
**pure JDBC** (no ORM, ever).

## STRUCTURE

```
nbp-project/
├── suds/         # Spring Boot 4.0.4 backend, port 8080. See suds/AGENTS.md
├── frontend/     # React 19 + Vite 5 SPA, port 5173 dev. See frontend/AGENTS.md
├── .sisyphus/    # Local Sisyphus plans/state (NOT shipped)
├── README.md     # User-facing setup instructions + wiki links
└── LICENSE
```

Two independent build systems. **Not a monorepo** — no workspaces, no shared
deps. Always `cd` into the right subdirectory before running commands.

## WHERE TO LOOK

| Task                      | Location                                  | Notes                                                                            |
| ------------------------- | ----------------------------------------- | -------------------------------------------------------------------------------- |
| Add backend endpoint      | `suds/`                                   | See `suds/AGENTS.md` 4-layer flow                                                |
| Add UI component          | `frontend/src/components/`                | Flat dir, JSX, Tailwind-styled                                                   |
| Wire FE → BE call         | `frontend/src/api.js`                     | Single axios instance, JWT interceptor                                           |
| Change DB schema          | None — schema lives on remote Oracle      | Hand-written DDL, no migrations in repo                                          |
| Configure DB / JWT secret | `suds/src/main/resources/application.yml` | Plaintext creds — **do not commit real ones**                                    |
| Configure FE API URL      | `frontend/.env` (from `.env.example`)     | Var is `VITE_API_BASE_URL`, code reads `VITE_API_URL` — **mismatch** (see NOTES) |
| Cross-cutting docs        | GitHub Wiki                               | Linked from `README.md`                                                          |

## CONVENTIONS (PROJECT-WIDE)

- **Bosnian/Croatian** for domain language: class names, method names, DB
  columns (`SLUCAJEVI`, `OSUMNJICENI`), variable names, comments, Javadoc,
  user-facing strings. **English only** for: framework keywords, exception
  messages thrown from `RuntimeException`.
- **Roles** are hard-coded enum-strings: `SEF_STANICE`, `INSPEKTOR`,
  `POLICAJAC`, `FORENZIČAR`. Spelling matters — frontend compares with `===`.
- **JWT** = HS256, claims `userId`, `role_name`, `stanica_id`. Frontend stores
  token in `localStorage`. Backend has token blacklist (`CRNA_LISTA_TOKENA`).
- **No CI/CD** in repo. No `.github/workflows`, no pre-commit hooks. Build,
  test, deploy is manual.
- **No shared types** — backend DTOs and frontend payloads drift independently.
  When changing a DTO, grep `frontend/src/api.js` and component callers.

## ANTI-PATTERNS (THIS PROJECT)

- **NEVER introduce an ORM** — JDBC purity is a course requirement, not taste.
  No Hibernate, no Spring Data JPA, no `@Entity`.
- **NEVER commit real DB credentials** to `application.yml` (currently checked
  in with course-server creds — replace before any public push).
- **NEVER add fields to `LANAC_NADZORA`** without preserving immutability —
  chain-of-custody rows are append-only by design.
- **NEVER assume monorepo tooling** — there's no root `package.json`,
  `turbo.json`, or workspace config. Don't try `npm install` at root.

## COMMANDS

```bash
# Backend
cd suds
mvn clean install            # build + run unit tests
mvn spring-boot:run          # serve :8080, Swagger at /swagger-ui.html

# Frontend
cd frontend
npm install
npm run dev                  # serve :5173 with HMR
npm run build                # produces frontend/dist/
npm run preview              # serves built bundle
```

No combined run script. Open two terminals.

## NOTES

- **Env var name mismatch (BUG-LIKE):** `frontend/.env.example` defines
  `VITE_API_BASE_URL`, but `frontend/src/api.js` reads `VITE_API_URL`. New
  contributors must rename the key in their `.env` or the SPA falls back to
  `http://localhost:8080/api`.
- `frontend/Dockerfile` exists (multi-stage Node + Nginx) but expects
  `REACT_APP_API_URL` (legacy CRA name) — likely stale. Vite build ignores it.
- Backend uses Spring Boot **4.0.4** (not 3.x LTS) — some Stack Overflow answers
  won't apply; check official docs.
- Frontend tests: dependencies present (`@testing-library/*`), but only
  `App.test.js` exists. No `npm test` script wired in `package.json`.
- Wiki (`github.com/Zukic98/NBP-project/wiki`) is the source of truth for
  architecture diagrams, ER diagram, API reference, role guides.
