package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KreirajSvjedokaRequest {
    private String imePrezime;
    private String jmbg;
    private String adresa;
    private String kontaktTelefon;
    private String biljeska;
}
