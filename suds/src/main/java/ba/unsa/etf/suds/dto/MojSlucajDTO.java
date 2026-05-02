package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/slucajevi/moji.
 *
 * <p>Sažeti prikaz slučaja u kojemu je prijavljeni uposlenik član tima.
 * Uključuje ulogu uposlenika na konkretnom slučaju.
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MojSlucajDTO {
    /** Primarni ključ slučaja. */
    private Long slucajId;

    /** Jedinstveni broj slučaja. */
    private String brojSlucaja;

    /** Tekstualni opis slučaja. */
    private String opis;

    /** Trenutni status slučaja. */
    private String status;

    /** Ime i prezime voditelja slučaja. */
    private String voditeljSlucaja;

    /** Uloga prijavljenog uposlenika na ovom slučaju. */
    private String ulogaNaSlucaju;

    /** Datum i vrijeme kreiranja slučaja. */
    private Timestamp datumKreiranja;
}
