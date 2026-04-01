package ba.unsa.etf.suds.ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Slucaj {
    private Long slucajId;        // SLUCAJ_ID
    private Long stanicaId;       // STANICA_ID
    private String brojSlucaja;   // BROJ_SLUCAJA
    private String opis;          // OPIS
    private String status;        // STATUS
    private Long voditeljUserId;  // VODITELJ_USER_ID
    private Timestamp datumKreiranja; // DATUM_KREIRANJA
}