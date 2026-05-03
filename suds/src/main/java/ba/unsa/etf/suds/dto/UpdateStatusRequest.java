package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Tijelo zahtjeva za PATCH /api/dokazi/{id}/status.
 *
 * <p>Ažurira status dokaza u lancu nadzora.
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdateStatusRequest {
    /** Novi status dokaza (npr. "U_LABORATORIJI", "NA_ČUVANJU", "VRAĆEN"). */
    private String status;
}
