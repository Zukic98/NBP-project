package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Tijelo zahtjeva za POST /api/slucajevi/{caseId}/dokazi.
 *
 * <p>JSON polja se šalju u snake_case formatu zahvaljujući
 * {@code @JsonNaming(SnakeCaseStrategy.class)}.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KreirajDokazRequest {
    /** Tekstualni opis dokaza. */
    private String opis;

    /** Lokacija na kojoj je dokaz pronađen. */
    private String lokacijaPronalaska;

    /** Tip dokaza (npr. "FIZIČKI", "DIGITALNI", "BIOLOŠKI"). */
    private String tipDokaza;
}
