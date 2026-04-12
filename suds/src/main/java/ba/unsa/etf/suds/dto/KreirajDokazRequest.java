package ba.unsa.etf.suds.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KreirajDokazRequest {
    private String opis;
    private String lokacijaPronalaska;
    private String tipDokaza;
}
