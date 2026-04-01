package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanacNadzora {
    private Long unosId;
    private Long dokazId;
    private Long stanicaId;
    private Timestamp datumPrimopredaje;
    private Long predaoUserId;
    private Long preuzeoUserId;
    private String svrhaPrimopredaje;
    private String potvrdaStatus;
    private String potvrdaNapomena;
    private Timestamp potvrdaDatum;
    private Long potvrdioUserId;
}
