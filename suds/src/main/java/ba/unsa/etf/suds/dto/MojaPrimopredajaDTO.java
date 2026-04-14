package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MojaPrimopredajaDTO {
    private Long unosId;
    private String dokazOpis;
    private String tipDokaza;
    private String preuzeoIme;
    private String svrhaPrimopredaje;
    private Timestamp datumPrimopredaje;
    private Long dokazId;
    private Long protekloSekundi;
}
