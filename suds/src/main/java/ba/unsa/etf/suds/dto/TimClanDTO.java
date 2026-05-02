package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Odgovor za GET /api/slucajevi/{id}/tim.
 *
 * <p>Podaci o jednom članu tima dodjeljenog slučaju.
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TimClanDTO {
    /** Primarni ključ dodjele iz tabele SLUCAJ_TIM. */
    private Long dodjelaId;

    /** ID uposlenika (NBP_USER). */
    private Long uposlenikId;

    /** Puno ime i prezime uposlenika. */
    private String imePrezime;

    /** Naziv sistemske uloge uposlenika (npr. "INSPEKTOR"). */
    private String nazivUloge;

    /** Uloga uposlenika na ovom konkretnom slučaju. */
    private String ulogaNaSlucaju;

    /** Broj značke uposlenika. */
    private String brojZnacke;

    /** Email adresa uposlenika. */
    private String email;
}
