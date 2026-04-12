package ba.unsa.etf.suds.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PrimopredajaZaPotvrduDTO {
    private Long unosId;
    private String dokazOpis;
    private String tipDokaza;
    private String predaoIme;
    private String svrhaPrimopredaje;
    private Timestamp datumPrimopredaje;
    private Long dokazId;
}
