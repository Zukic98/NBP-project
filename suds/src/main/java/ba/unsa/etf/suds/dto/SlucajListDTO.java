package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/slucajevi.
 *
 * <p>Sažeti prikaz jednog slučaja namijenjen listama u SPA-u.
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SlucajListDTO {
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

    /** Datum i vrijeme kreiranja slučaja. */
    private Timestamp datumKreiranja;
}
