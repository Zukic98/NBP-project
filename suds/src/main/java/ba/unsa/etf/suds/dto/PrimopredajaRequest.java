package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Tijelo zahtjeva za POST /api/dokazi/{id}/primopredaja.
 *
 * <p>Inicira primopredaju dokaza direktno s ID-om dokaza u putanji.
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PrimopredajaRequest {
    /** ID uposlenika koji preuzima dokaz. */
    private Long preuzeoUposlenikId;

    /** Svrha primopredaje dokaza. */
    private String svrha;
}
