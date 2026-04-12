package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DokazListDTO {
    private Long dokazId;
    private String opis;
    private String lokacijaPronalaska;
    private String tipDokaza;
    private String status;
    private String prikupioIme;
    private Timestamp datumPrikupa;
    private Long slucajId;
}
