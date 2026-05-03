package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Tijelo zahtjeva za POST /api/slucajevi/{id}/svjedoci ili POST /api/svjedoci.
 *
 * <p>Kreira novog svjedoka i vezuje ga za slučaj. JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KreirajSvjedokaRequest {
    /** Puno ime i prezime svjedoka. */
    private String imePrezime;

    /** Jedinstveni matični broj građana svjedoka. */
    private String jmbg;

    /** Adresa stanovanja svjedoka. */
    private String adresa;

    /** Kontakt telefon svjedoka. */
    private String kontaktTelefon;

    /** Dodatne bilješke o svjedoku ili iskazu. */
    private String biljeska;
}
