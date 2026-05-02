package ba.unsa.etf.suds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Glavna ulazna tačka SUDS Spring Boot aplikacije.
 * <p>
 * Pokreće cijeli aplikacijski kontekst, uključujući sve komponente unutar
 * paketa {@code ba.unsa.etf.suds} (kontroleri, servisi, repozitoriji,
 * sigurnosna konfiguracija i upravljanje bazom podataka).
 * </p>
 */
@SpringBootApplication
@ComponentScan(basePackages = "ba.unsa.etf.suds") 
public class SudsApplication {
    /**
     * Pokreće Spring Boot aplikaciju.
     *
     * @param args argumenti komandne linije proslijeđeni JVM-u
     */
    public static void main(String[] args) {
        SpringApplication.run(SudsApplication.class, args);
    }
}