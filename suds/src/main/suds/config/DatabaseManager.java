package ba.unsa.etf.suds.ba.unsa.etf.suds.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
     * Open and return new clean JDBC connection to Oracle database.
     * IMPORTANT: Person who calls this method must close connection.
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