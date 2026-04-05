# PROJECT KNOWLEDGE BASE

**Generated:** 2026-04-05 **Commit:** f600f29 **Branch:** main

## OVERVIEW

SUDS (Sistem za upravljanje dokazima i slucajevima) — law enforcement evidence &
case management REST API. Java 21 + Spring Boot 4.0.4 + **pure JDBC** against
Oracle DB. No ORM.

## STRUCTURE

```
suds/
├── src/main/java/ba/unsa/etf/suds/
│   ├── config/          # DatabaseManager (JDBC connection factory)
│   ├── controller/      # 10 REST controllers (@RestController)
│   ├── dto/             # 3 DTOs for complex join responses
│   ├── model/           # 14 Lombok POJOs mapping Oracle tables
│   ├── repository/      # 10 repositories (raw SQL via PreparedStatement)
│   └── service/         # 10 services (thin business logic)
├── src/main/resources/
│   └── application.yml  # DB creds + server port
└── src/test/java/.../service/  # 3 unit tests (Mockito)
```

## WHERE TO LOOK

| Task                    | Location                                                     | Notes                                                           |
| ----------------------- | ------------------------------------------------------------ | --------------------------------------------------------------- |
| Add new entity          | `model/` → `repository/` → `service/` → `controller/`        | Follow Slucaj\* as reference pattern                            |
| Add complex query       | `repository/`                                                | See `SlucajRepository.findDetaljiByBroj` for JOIN + DTO mapping |
| Add DTO for joined data | `dto/` + `repository/`                                       | DTO is plain Lombok, repo does the SQL join                     |
| DB connection issues    | `config/DatabaseManager.java`                                | Single connection factory, caller must close                    |
| Configure DB            | `src/main/resources/application.yml`                         | `db.url`, `db.username`, `db.password`                          |
| Add tests               | `src/test/java/.../service/`                                 | JUnit 5 + Mockito, mock repository layer                        |
| Auth/user models        | `model/NbpUser.java`, `NbpRole.java`, `CrnaListaTokena.java` | Token blacklist model exists, no auth controller yet            |

## CONVENTIONS

- **Pure JDBC only.** No Hibernate, no Spring Data JPA, no `@Entity`. Models are
  plain POJOs.
- **Bosnian/Croatian naming** for domain classes, methods, test names, Javadoc.
  DB column names are uppercase Bosnian.
- **Constructor injection** everywhere (no `@Autowired` annotation).
- **Lombok** `@Data @NoArgsConstructor @AllArgsConstructor` on all models.
  `@Data` only on DTOs.
- **Repository pattern**: each repo injects `DatabaseManager`, gets connection
  per query via try-with-resources, wraps SQLExceptions in `RuntimeException`.
- **Controller pattern**: inject service, return `ResponseEntity<T>`. API
  prefix: `/api/{plural-bosnian-noun}`.
- **Error messages** in English for RuntimeExceptions, Bosnian in Javadoc and
  user-facing strings.
- **No field injection**, no `@Autowired`.

## ANTI-PATTERNS (THIS PROJECT)

- **NEVER use ORM** — project requirement is pure JDBC.
- **NEVER commit DB credentials** — `application.yml` has placeholder values.
- **ALWAYS close connections** — `DatabaseManager.getConnection()` returns raw
  connection; caller owns lifecycle. Use try-with-resources.
- **NEVER use `@Autowired`** — all injection is via constructor.

## REPOSITORY LAYER TEMPLATE

Every repository follows this exact pattern:

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
            throw new RuntimeException("Error while ...", e);
        }
    }

    // findAll, findById (Optional<T>), update, delete follow same pattern
    // mapRowToXxx(ResultSet rs) helper at bottom
}
```

## DB SCHEMA HINTS

- Tables: `SLUCAJEVI`, `OSUMNJICENI`, `KRIVICNA_DJELA`, `DOKAZI`, `STANICE`,
  `LANAC_NADZORA`, `FORENZICKI_IZVJESTAJI`, `ADRESE`, `SVJEDOCI`,
  `UPOSLENIK_PROFIL`
- Junction tables: `SLUCAJ_OSUMNJICENI`, `SLUCAJ_KRIVICNO_DJELO`
- Auth tables (shared schema `nbp`): `NBP_USER`, `NBP_ROLE`,
  `CRNA_LISTA_TOKENA`, `NBP_LOG`
- Column naming: `UPPERCASE_BOSNIAN` (e.g., `DATUM_KREIRANJA`, `IME_PREZIME`,
  `BROJ_SLUCAJA`)

## COMMANDS

```bash
mvn clean install          # Build + run tests
mvn spring-boot:run        # Start on :8080
mvn test                   # Unit tests only (JUnit 5 + Mockito)
```

## NOTES

- Spring Boot **4.0.4** (cutting-edge, not LTS). Surefire uses
  `-XX:+EnableDynamicAgentLoading` for Mockito.
- Models have 4 extra auth-related classes (`NbpUser`, `NbpRole`,
  `CrnaListaTokena`, `NbpLog`) without corresponding controllers/services — auth
  layer likely planned but not yet implemented.
- `DatabaseManager.getConnection()` creates a **new connection per call** — no
  pooling. Acceptable for course project, not production.
- Test coverage is minimal: 3 service tests out of 10 services.
