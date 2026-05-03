package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

/**
 * POJO koji reprezentira jedan red u tabeli OSUMNJICENI — osoba koja je
 * osumnjičena za jedno ili više krivičnih djela.
 *
 * <p>Veže se na: ADRESE (kroz ADRESA_ID). Povezuje se sa slučajevima kroz
 * veznu tabelu SLUCAJ_OSUMNJICENI. Nadređeni entitet za OSUMNJICENI_FOTOGRAFIJE.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Osumnjiceni {
    /** Primarni ključ — kolona OSUMNJICENI_ID. */
    private Long osumnjiceniId; // OSUMNJICENI_ID

    /** Puno ime i prezime osumnjičenog — kolona IME_PREZIME. */
    private String imePrezime;  // IME_PREZIME

    /** Jedinstveni matični broj građana — kolona JMBG. */
    private String jmbg;        // JMBG

    /** FK na ADRESE.ADRESA_ID — adresa stanovanja osumnjičenog — kolona ADRESA_ID. */
    private Long adresaId;      // ADRESA_ID

    /** Datum rođenja osumnjičenog — kolona DATUM_RODJENJA (tip DATE u Oracle). */
    private Date datumRodjenja; // DATUM_RODJENJA
}