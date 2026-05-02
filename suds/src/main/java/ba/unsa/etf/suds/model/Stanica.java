package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli STANICE — policijska stanica
 * koja je organizaciona jedinica sistema.
 *
 * <p>Veže se na: ADRESE (kroz ADRESA_ID). Nadređeni entitet za SLUCAJEVI,
 * DOKAZI, LANAC_NADZORA i UPOSLENIK_PROFIL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stanica {
    /** Primarni ključ — kolona STANICA_ID. */
    private Long stanicaId;       // U bazi: STANICA_ID

    /** Naziv policijske stanice — kolona IME_STANICE. */
    private String imeStanice;    // U bazi: IME_STANICE

    /** FK na ADRESE.ADRESA_ID — adresa stanice — kolona ADRESA_ID. */
    private Long adresaId;        // U bazi: ADRESA_ID

    /** Datum i vrijeme kreiranja zapisa o stanici — kolona DATUM_KREIRANJA. */
    private Timestamp datumKreiranja; // U bazi: DATUM_KREIRANJA
}