# BACKEND KNOWLEDGE BASE (suds/)

**Generated:** 2026-05-02 **Commit:** b7a8aea **Stack:** Java 21 + Spring Boot
4.0.4

## OVERVIEW

REST API for SUDS. **Pure JDBC** against Oracle 19c (no Hibernate, no Spring
Data JPA, no `@Entity`). 4-layer onion:
`controller → service → repository → DatabaseManager`. JWT (HS256) auth via
Spring Security + custom `JwtFilter`.

## STRUCTURE

```
suds/
├── src/main/java/ba/unsa/etf/suds/
│   ├── config/          # 3: DatabaseManager (JDBC factory), SecurityConfig, OpenApiConfig
│   ├── controller/      # 17 @RestController classes
│   ├── dto/             # 33 request/response DTOs (incl. 4 photo DTOs, 5 chain-of-custody DTOs)
│   ├── model/           # 18 Lombok POJOs mapping Oracle tables (incl. 4 NBP_* auth tables)
│   ├── repository/      # 17 raw-SQL repos via PreparedStatement
│   ├── service/         # 16 services (thin business logic + PdfGeneratorService)
│   ├── security/        # 2: JwtFilter (OncePerRequestFilter), JwtUtil
│   └── SudsApplication.java
├── src/main/resources/
│   ├── application.yml  # DB creds + JWT secret (CHECKED-IN — replace before push)
│   └── fonts/           # iText 7 PDF generation fonts
└── src/test/java/.../
    ├── service/         # 10 Mockito unit tests
    ├── repository/      # 3 repo tests (DB-touching)
    └── controller/      # 3 incl. RBAC test (SlucajControllerRbacTest)
```

## WHERE TO LOOK

| Task                             | Location                                                      | Notes                                                             |
| -------------------------------- | ------------------------------------------------------------- | ----------------------------------------------------------------- |
| Add new entity                   | `model/` → `repository/` → `service/` → `controller/`         | Follow `Slucaj*` chain as reference                               |
| Add complex JOIN query           | `repository/`                                                 | See `SlucajRepository.findDetaljiByBroj` for JOIN→DTO map         |
| Add DTO for joined data          | `dto/` + `repository/` mapper                                 | Plain Lombok `@Data`; SQL does the join                           |
| DB connection                    | `config/DatabaseManager.java`                                 | New connection per call; **caller closes via try-with-resources** |
| JWT auth wiring                  | `security/JwtFilter.java`, `security/JwtUtil.java`            | Reads `Authorization: Bearer …`; checks `CRNA_LISTA_TOKENA`       |
| Add public (no-auth) endpoint    | `config/SecurityConfig.java`                                  | Whitelist path; `/auth/login` and `/stanice/register` are public  |
| Login / logout / password change | `controller/AuthController.java` + `service/AuthService.java` | Logout writes token to blacklist                                  |
| Configure DB / JWT secret        | `src/main/resources/application.yml`                          | `db.*`, `jwt.secret`, `jwt.expiration-ms`                         |
| PDF report (case)                | `service/PdfGeneratorService.java`                            | iText 7; fonts under `resources/fonts/`                           |
| Add tests                        | `src/test/java/.../{service,repository,controller}/`          | JUnit 5 + Mockito; mock repository in service tests               |

## CONVENTIONS

- **Pure JDBC.** No ORM. Models are plain POJOs, no `@Entity`, no `@Column`.
- **Constructor injection** everywhere. **No `@Autowired`** annotation. Lombok's
  `@RequiredArgsConstructor` is acceptable; field injection is not.
- **Lombok** `@Data @NoArgsConstructor @AllArgsConstructor` on `model/` POJOs.
  `@Data` (or just getters) on `dto/` records.
- **Repository pattern**: inject `DatabaseManager`; per-call connection via
  `try (Connection conn = dbManager.getConnection(); PreparedStatement …)`; wrap
  `SQLException` in `RuntimeException` with English message.
- **Controller pattern**: inject service, return `ResponseEntity<T>`. URL prefix
  `/api/{plural-bosnian-noun}` (e.g. `/api/slucajevi`, `/api/dokazi`).
- **Bosnian/Croatian** for class/method/variable names, Javadoc, user-facing
  messages. **English** only for `RuntimeException` messages and framework
  keywords.
- **Javadoc in Bosnian** with `<p>`/`<ol>` HTML tags — see
  `security/JwtFilter.java` for the canonical style.
- **JWT claims**: `userId` (long), `role_name` (string), `stanica_id` (long).
  Filter sets `ROLE_<role_name>` authority.

## ANTI-PATTERNS (REPEATING SINCE THEY MATTER HERE)

- **NEVER use ORM** — course requirement, not preference.
- **NEVER use `@Autowired`** — constructor injection only.
- **NEVER leak `Connection`** — every `DatabaseManager.getConnection()` MUST be
  in a try-with-resources. There is **no pool** — leaks exhaust the Oracle
  account fast.
- **NEVER mutate `LANAC_NADZORA` rows** — chain-of-custody is append-only.
  Updates/deletes are forbidden by domain rules. Add a new row with reversed
  roles instead.
- **NEVER hardcode role strings** outside the canonical four: `SEF_STANICE`,
  `INSPEKTOR`, `POLICAJAC`, `FORENZIČAR`. Mismatched spelling silently fails
  RBAC.

## REPOSITORY TEMPLATE

```java
@Repository
public class XxxRepository {
    private final DatabaseManager dbManager;

    public XxxRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void save(Xxx entity) {
        String sql = "INSERT INTO TABLE_NAME (...) VALUES (?, ?, ...)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // stmt.setXxx(1, entity.getField());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error while saving Xxx", e);
        }
    }
    // findAll, findById (returns Optional<T>), update, delete same shape.
    // Private mapRowToXxx(ResultSet rs) helper at file bottom.
}
```

## DB SCHEMA HINTS

- Domain tables: `SLUCAJEVI`, `OSUMNJICENI`, `KRIVICNA_DJELA`, `DOKAZI`,
  `STANICE`, `LANAC_NADZORA` (append-only), `FORENZICKI_IZVJESTAJI`, `ADRESE`,
  `SVJEDOCI`, `UPOSLENIK_PROFIL`, `TIM_NA_SLUCAJU`, `DOKAZ_FOTOGRAFIJA`,
  `OSUMNJICENI_FOTOGRAFIJA`
- Junction: `SLUCAJ_OSUMNJICENI`, `SLUCAJ_KRIVICNO_DJELO`
- Auth (separate `nbp` schema): `NBP_USER`, `NBP_ROLE`, `CRNA_LISTA_TOKENA`,
  `NBP_LOG`
- Columns: `UPPERCASE_BOSNIAN` (`DATUM_KREIRANJA`, `IME_PREZIME`,
  `BROJ_SLUCAJA`). Use exact casing in SQL — Oracle is case-sensitive when
  identifiers are quoted, and the schema does quote them.
- **No migrations in repo.** DDL lives on the remote Oracle server. Schema
  changes coordinate via the wiki ER diagram.

## COMMANDS

```bash
mvn clean install           # build + run unit tests
mvn spring-boot:run         # serve :8080, Swagger at /swagger-ui.html
mvn test                    # unit tests only
```

## NOTES

- Spring Boot **4.0.4** (not 3.x LTS). Many SO answers don't apply — check
  official docs first.
- Surefire plugin uses `-XX:+EnableDynamicAgentLoading` argLine for Mockito on
  Java 21. Don't strip it.
- Maven Javadoc plugin is configured (`failOnError=false`, Bosnian title).
  `mvn javadoc:javadoc` emits to `target/reports/apidocs/` — that subtree is
  generated, not source.
- `application.yml` currently contains live course-server creds (`NBPT5/nbpt5`,
  `ora-02.db.lab.etf.unsa.ba`). **Replace before any public push.**
- `DatabaseManager` makes a fresh `DriverManager.getConnection` each call —
  acceptable for the course, never for production.
- Test coverage: 10 service + 3 repo + 3 controller. `AuthControllerTest` and
  `SlucajControllerRbacTest` are the RBAC reference.
