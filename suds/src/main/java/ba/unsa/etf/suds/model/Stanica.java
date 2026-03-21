package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stanica {
    private Long stanicaId;       // U bazi: STANICA_ID
    private String imeStanice;    // U bazi: IME_STANICE
    private Long adresaId;        // U bazi: ADRESA_ID
    private Timestamp datumKreiranja; // U bazi: DATUM_KREIRANJA
}