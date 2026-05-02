package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/slucajevi/{id}/dokazi i GET /api/dokazi.
 *
 * <p>Sažeti prikaz jednog dokaza namijenjen listama u SPA-u.
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DokazListDTO {
    /** Primarni ključ dokaza iz tabele DOKAZI. */
    private Long dokazId;

    /** Tekstualni opis dokaza. */
    private String opis;

    /** Lokacija na kojoj je dokaz pronađen. */
    private String lokacijaPronalaska;

    /** Tip dokaza (npr. "FIZIČKI", "DIGITALNI"). */
    private String tipDokaza;

    /** Trenutni status dokaza u lancu nadzora. */
    private String status;

    /** Ime i prezime uposlenika koji je prikupio dokaz. */
    private String prikupioIme;

    /** Datum i vrijeme prikupljanja dokaza. */
    private Timestamp datumPrikupa;

    /** ID slučaja kojemu dokaz pripada. */
    private Long slucajId;
}
