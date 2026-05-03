package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Tijelo zahtjeva za POST /api/slucajevi/{id}/tim.
 *
 * <p>Dodaje postojećeg uposlenika u tim slučaja s određenom ulogom.
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DodajClanaTRequest {
    /** ID uposlenika (NBP_USER) koji se dodaje u tim. */
    private Long uposlenikId;

    /** Uloga uposlenika na ovom slučaju (npr. "VODITELJ", "ISTRAŽITELJ"). */
    private String ulogaNaSlucaju;
}
