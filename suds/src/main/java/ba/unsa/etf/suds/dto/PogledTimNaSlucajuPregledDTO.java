package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * Odgovor za GET /api/pogledi/tim-na-slucaju.
 *
 * <p>Jedan red pogleda {@code POGLED_TIM_NA_SLUCAJU_PREGLED} — član tima
 * dodijeljen na slučaj, sa sistemskom ulogom (iz {@code nbp.NBP_ROLE})
 * i ulogom na slučaju, te imenom stanice u kojoj se slučaj vodi.
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PogledTimNaSlucajuPregledDTO {
    private Long dodjelaId;
    private Long slucajId;
    private String brojSlucaja;
    private String statusSlucaja;
    private Long stanicaId;
    private String imeStanice;
    private Long userId;
    private String clanIme;
    private String sistemskaUloga;
    private String ulogaNaSlucaju;
}
