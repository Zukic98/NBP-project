package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MojSlucajDTO {
    private Long slucajId;
    private String brojSlucaja;
    private String opis;
    private String status;
    private String voditeljSlucaja;
    private String ulogaNaSlucaju;
    private Timestamp datumKreiranja;
}
