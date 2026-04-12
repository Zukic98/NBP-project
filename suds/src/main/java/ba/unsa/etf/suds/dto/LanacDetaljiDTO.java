package ba.unsa.etf.suds.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LanacDetaljiDTO {
    private Long unosId;
    private Timestamp datumPrimopredaje;
    private String predaoIme;
    private String preuzeoIme;
    private String svrhaPrimopredaje;
    private String potvrdaStatus;
    private String potvrdaNapomena;
    private Timestamp potvrdaDatum;
    private String potvrdioIme;
}
