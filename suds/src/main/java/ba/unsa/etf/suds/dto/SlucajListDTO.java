package ba.unsa.etf.suds.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SlucajListDTO {
    private Long slucajId;
    private String brojSlucaja;
    private String opis;
    private String status;
    private String voditeljSlucaja;
    private Timestamp datumKreiranja;
}
