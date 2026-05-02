package ba.unsa.etf.suds.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fabrika JDBC konekcija prema Oracle bazi podataka.
 * <p>
 * Svaki poziv metode {@link #getConnection()} kreira <strong>novu</strong>
 * konekciju putem {@link DriverManager} — nema connection pool-a.
 * Pozivalac je vlasnik konekcije i <strong>mora</strong> je zatvoriti,
 * idealno putem {@code try-with-resources} bloka:
 * </p>
 * <pre>{@code
 * try (Connection conn = dbManager.getConnection();
 *      PreparedStatement stmt = conn.prepareStatement(sql)) {
 *     // ...
 * }
 * }</pre>
 * <p>
 * Parametri konekcije ({@code db.url}, {@code db.username}, {@code db.password})
 * čitaju se iz {@code application.yml}.
 * </p>
 */
@Configuration
public class DatabaseManager {

    // Spring Boot will automatically pull these values from application.yml
    @Value("${db.url}")
    private String url;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    /**
     * Otvara i vraća novu JDBC konekciju prema Oracle bazi podataka.
     * <p>
     * <strong>Pravilo vlasništva:</strong> pozivalac je odgovoran za zatvaranje
     * vraćene konekcije. Preporučuje se upotreba {@code try-with-resources}
     * kako bi se konekcija sigurno oslobodila čak i u slučaju iznimke.
     * </p>
     *
     * @return aktivna {@link Connection} prema Oracle bazi
     * @throws SQLException ako Oracle JDBC drajver nije pronađen na classpath-u
     *                      ili ako konekcija ne može biti uspostavljena
     */
    public Connection getConnection() throws SQLException {
        try {
            // Ensure that Oracle driver is read in memory
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC Driver nije pronađen! Provjeri pom.xml", e);
        }

        // return active connection
        return DriverManager.getConnection(url, username, password);
    }
}