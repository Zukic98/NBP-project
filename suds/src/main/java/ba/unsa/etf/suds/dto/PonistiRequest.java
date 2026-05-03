package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Tijelo zahtjeva za DELETE /api/primopredaje/{unosId}/ponisti.
 *
 * <p>Poništava primopredaju koja još nije potvrđena. Razlog poništavanja
 * se bilježi u sistemu. JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PonistiRequest {
    /** Razlog poništavanja primopredaje. */
    private String razlog;
}
