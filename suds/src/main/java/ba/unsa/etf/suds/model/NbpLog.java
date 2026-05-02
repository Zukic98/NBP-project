package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli nbp.NBP_LOG — audit log koji bilježi
 * DML operacije (INSERT, UPDATE, DELETE) nad tabelama u bazi podataka.
 *
 * <p>Popunjava se automatski kroz Oracle triggere; ne upisuje se direktno iz aplikacije.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NbpLog {
    /** Primarni ključ — kolona ID. */
    private Long id;

    /** Naziv DML akcije koja je izvršena (npr. "INSERT", "UPDATE") — kolona ACTION_NAME. */
    private String actionName;

    /** Naziv tabele nad kojom je akcija izvršena — kolona TABLE_NAME. */
    private String tableName;

    /** Datum i vrijeme izvršavanja akcije — kolona DATE_TIME. */
    private Timestamp dateTime;

    /** Korisničko ime DB sesije koja je izvršila akciju — kolona DB_USER. */
    private String dbUser;
}