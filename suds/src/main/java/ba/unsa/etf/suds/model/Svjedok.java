package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO koji reprezentira jedan red u tabeli SVJEDOCI — svjedok koji je
 * vezan za određeni krivični slučaj.
 *
 * <p>Veže se na: SLUCAJEVI (kroz SLUCAJ_ID), ADRESE (kroz ADRESA_ID).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Svjedok {
    /** Primarni ključ — kolona SVJEDOK_ID. */
    private Long svjedokId;

    /** FK na SLUCAJEVI.SLUCAJ_ID — slučaj za koji svjedok svjedoči — kolona SLUCAJ_ID. */
    private Long slucajId;

    /** Puno ime i prezime svjedoka — kolona IME_PREZIME. */
    private String imePrezime;

    /** Jedinstveni matični broj građana svjedoka — kolona JMBG. */
    private String jmbg;

    /** FK na ADRESE.ADRESA_ID — adresa stanovanja svjedoka — kolona ADRESA_ID. */
    private Long adresaId;

    /** Kontakt telefon svjedoka — kolona KONTAKT_TELEFON. */
    private String kontaktTelefon;

    /** Slobodna bilješka inspektora o svjedoku — kolona BILJESKA. */
    private String biljeska;
}
