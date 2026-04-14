package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TimClanDTO {
    private Long dodjelaId;
    private Long uposlenikId;
    private String imePrezime;
    private String nazivUloge;
    private String ulogaNaSlucaju;
    private String brojZnacke;
    private String email;
}
