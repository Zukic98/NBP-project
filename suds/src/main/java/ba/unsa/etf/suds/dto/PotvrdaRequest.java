package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Tijelo zahtjeva za PATCH /api/lanac-nadzora/{unosId}/potvrda.
 *
 * <p>Primalac dokaza potvrđuje ili odbija primopredaju. Napomena je
 * obavezna pri odbijanju. JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PotvrdaRequest {
    /** Status potvrde: "POTVRĐENO" ili "ODBIJENO". */
    private String status;

    /** Napomena primaoca uz potvrdu ili odbijanje primopredaje. */
    private String napomena;
}
