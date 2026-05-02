package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Odgovor za GET /api/slucajevi/{id}/svjedoci i GET /api/svjedoci/{id}.
 *
 * <p>Sažeti podaci o svjedoku. JSON polja su u snake_case formatu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SvjedokDTO {
    /** Primarni ključ svjedoka iz tabele SVJEDOCI. */
    private Long svjedokId;

    /** Puno ime i prezime svjedoka. */
    private String imePrezime;

    /** Kontakt telefon svjedoka. */
    private String kontaktTelefon;

    /** Adresa stanovanja svjedoka. */
    private String adresa;

    /** Jedinstveni matični broj građana svjedoka. */
    private String jmbg;

    /** Bilješka o svjedoku ili iskazu. */
    private String biljeska;
}
