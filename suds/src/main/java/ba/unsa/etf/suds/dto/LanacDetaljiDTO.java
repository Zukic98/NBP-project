package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/dokazi/{id}/lanac-nadzora.
 *
 * <p>Detalji jednog unosa u lancu nadzora dokaza, uključujući informacije
 * o primopredaji i statusu potvrde. JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LanacDetaljiDTO {
    /** Primarni ključ unosa u lancu nadzora. */
    private Long unosId;

    /** Datum i vrijeme primopredaje dokaza. */
    private Timestamp datumPrimopredaje;

    /** Ime i prezime uposlenika koji je predao dokaz. */
    private String predaoIme;

    /** Ime i prezime uposlenika koji je preuzeo dokaz. */
    private String preuzeoIme;

    /** Svrha primopredaje dokaza. */
    private String svrhaPrimopredaje;

    /** Status potvrde primopredaje (npr. "POTVRĐENO", "NA_ČEKANJU", "ODBIJENO"). */
    private String potvrdaStatus;

    /** Napomena primaoca uz potvrdu ili odbijanje primopredaje. */
    private String potvrdaNapomena;

    /** Datum i vrijeme potvrde ili odbijanja primopredaje. */
    private Timestamp potvrdaDatum;

    /** Ime i prezime uposlenika koji je potvrdio ili odbio primopredaju. */
    private String potvrdioIme;
}
