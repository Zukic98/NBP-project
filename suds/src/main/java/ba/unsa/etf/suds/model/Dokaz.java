package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dokaz {
    private Long dokazId;
    private Long slucajId;
    private Long stanicaId;
    private String opis;
    private String lokacijaPronalaska;
    private String tipDokaza;
    private String status;
    private Timestamp datumPrikupa;
    private Long prikupioUserId;
}