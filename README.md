# SUDS — Sistem za Upravljanje Dokazima i Slučajevima

**Evidence and Case Management System** — a full-stack web application for
law-enforcement agencies, digitalizing criminal cases, suspects, witnesses and
the legally-critical **chain of custody** for forensic evidence.

Built for the **Napredne baze podataka** (Advanced Databases) course at ETF
Sarajevo, the backend talks to Oracle via **pure JDBC** — no Hibernate, no
Spring Data JPA.

> 📚 **Full documentation lives in the
> [project wiki](https://github.com/Zukic98/NBP-project/wiki).**

______________________________________________________________________

## 🚓 About the project

SUDS solves three concrete problems for police stations:

1. **Fragmented case data** — cases, witnesses, suspects and evidence used to
   live in separate paper binders. SUDS keeps everything related to a case under
   one record and joins the rest by foreign key.
2. **Untraceable evidence handovers** — every transfer between officers, lab and
   depot is recorded as an immutable `LANAC_NADZORA` row with sender, receiver,
   station, timestamp and confirmation status. See
   **[Chain of Custody](https://github.com/Zukic98/NBP-project/wiki/09-Chain-of-Custody)**.
3. **Inconsistent role responsibilities** — four roles (`SEF_STANICE`,
   `INSPEKTOR`, `POLICAJAC`, `FORENZIČAR`) enforced by Spring Security and the
   React UI.

## ✨ Key features

- **Case management** — create, assign lead investigator, generate per-case PDF
  report (iText 7).
- **Suspect tracking** — register suspects with photos, link to one or more
  cases.
- **Evidence & chain of custody** — handovers require explicit confirmation by
  the receiver.
- **Forensic workflow** — `FORENZIČAR`-only reports tied to specific evidence.
- **Personnel management** — `SEF_STANICE` onboards and deactivates employees in
  their station.
- **Live API docs** — Swagger UI at `/swagger-ui.html` (SpringDoc OpenAPI 2.1).

## 🛠️ Tech stack

| Layer       | Stack                                                             |
| ----------- | ----------------------------------------------------------------- |
| Frontend    | React 19 + Vite 5 + TailwindCSS 3 + axios                         |
| Backend     | Java 21 + Spring Boot 4.0.4 + Spring Security + JWT (jjwt 0.12.6) |
| Persistence | Pure JDBC (`java.sql.*`) over Oracle 19c (`ojdbc11`)              |
| Reporting   | iText 7 (server-side PDF), html2pdf.js (client-side)              |
| API docs    | SpringDoc OpenAPI 2.1.0                                           |
| Build tools | Maven, Vite                                                       |

## 🏛️ Architecture

A classic 3-tier backend (`controller` → `service` → `repository`) plus a
stateless React SPA. Authentication is JWT (HS256) carrying `user_id`,
`role_name` and `stanica_id` claims.

```text
nbp-project/
├── suds/        ← Spring Boot backend (port 8080)
└── frontend/    ← React + Vite SPA (port 5173 in dev)
```

For full diagrams, see
**[Architecture](https://github.com/Zukic98/NBP-project/wiki/03-Architecture)**.

## 📥 Quick start

### Prerequisites

- JDK 21 (JDK 26 also works)
- Maven 3.9+
- Node.js 18+
- An Oracle DB account (course server: `ora-02.db.lab.etf.unsa.ba:1521/ETFDB`)

### 1. Clone

```bash
git clone https://github.com/Zukic98/NBP-project.git
cd NBP-project
```

### 2. Configure & run the backend

Edit `suds/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

db:
  url: jdbc:oracle:thin:@//YOUR_HOST:1521/YOUR_SERVICE_NAME
  username: YOUR_USERNAME
  password: YOUR_PASSWORD

jwt:
  secret: change-me-to-a-long-random-256-bit-string
  expiration-ms: 86400000
```

Then:

```bash
cd suds
mvn clean install
mvn spring-boot:run
```

API: <http://localhost:8080> Swagger UI: <http://localhost:8080/swagger-ui.html>

### 3. Configure & run the frontend

```bash
cd frontend
cp .env.example .env       # rename VITE_API_BASE_URL → VITE_API_URL inside .env
npm install
npm run dev
```

SPA: <http://localhost:5173>

> Detailed step-by-step instructions, including the first-run bootstrap flow,
> are in the
> **[Setup Guide](https://github.com/Zukic98/NBP-project/wiki/04-Setup-Guide)**.

## 📖 Documentation

The full project documentation lives in the
**[GitHub Wiki](https://github.com/Zukic98/NBP-project/wiki)**:

| Section                                                                                                       | Content                                                                                                                                                                                                                                                                                                                                                              |
| ------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| [About the Project](https://github.com/Zukic98/NBP-project/wiki/01-About-The-Project)                         | What SUDS does and why                                                                                                                                                                                                                                                                                                                                               |
| [Tech Stack](https://github.com/Zukic98/NBP-project/wiki/02-Tech-Stack)                                       | Every dependency, version, purpose                                                                                                                                                                                                                                                                                                                                   |
| [Architecture](https://github.com/Zukic98/NBP-project/wiki/03-Architecture)                                   | System diagrams (Mermaid), 3-tier, request lifecycle                                                                                                                                                                                                                                                                                                                 |
| [Setup Guide](https://github.com/Zukic98/NBP-project/wiki/04-Setup-Guide)                                     | Prereqs, step-by-step install                                                                                                                                                                                                                                                                                                                                        |
| [Configuration](https://github.com/Zukic98/NBP-project/wiki/05-Configuration)                                 | Every config key explained                                                                                                                                                                                                                                                                                                                                           |
| [Database Schema](https://github.com/Zukic98/NBP-project/wiki/06-Database-Schema)                             | Mermaid ER diagram + every table                                                                                                                                                                                                                                                                                                                                     |
| [API Reference](https://github.com/Zukic98/NBP-project/wiki/07-API-Reference)                                 | Every endpoint, examples, status codes                                                                                                                                                                                                                                                                                                                               |
| [Authentication & Authorization](https://github.com/Zukic98/NBP-project/wiki/08-Authentication-Authorization) | JWT claims, login flow, role rules                                                                                                                                                                                                                                                                                                                                   |
| [Chain of Custody](https://github.com/Zukic98/NBP-project/wiki/09-Chain-of-Custody)                           | The core domain workflow                                                                                                                                                                                                                                                                                                                                             |
| [Frontend Architecture](https://github.com/Zukic98/NBP-project/wiki/10-Frontend-Architecture)                 | React component tree                                                                                                                                                                                                                                                                                                                                                 |
| User guides                                                                                                   | Per-role walkthroughs ([Šef stanice](https://github.com/Zukic98/NBP-project/wiki/11-User-Guide-Sef-Stanice), [Inspektor](https://github.com/Zukic98/NBP-project/wiki/12-User-Guide-Inspektor), [Policajac](https://github.com/Zukic98/NBP-project/wiki/13-User-Guide-Policajac), [Forenzičar](https://github.com/Zukic98/NBP-project/wiki/14-User-Guide-Forenzicar)) |

## 🧪 Live API documentation

When the backend is running, SpringDoc serves:

- **Swagger UI:** <http://localhost:8080/swagger-ui.html> — interactive,
  click-to-call.
- **OpenAPI JSON:** <http://localhost:8080/v3/api-docs> — machine-readable,
  ready for Postman / client codegen.

Both are public (no token required) and authenticated operations have an
*Authorize* button that takes a JWT.

## 📜 License

See [LICENSE](LICENSE).
