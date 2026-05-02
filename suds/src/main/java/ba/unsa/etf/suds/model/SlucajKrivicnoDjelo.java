package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO koji reprezentira jedan red u veznoj tabeli SLUCAJ_KRIVICNO_DJELO —
 * spaja tabele SLUCAJEVI i KRIVICNA_DJELA u relaciji više-prema-više.
 *
 * <p>Polja {@code nazivDjela}, {@code kategorija} i {@code kazneniZakonClan}
 * nisu kolone ove tabele — popunjavaju se JOIN-om pri čitanju radi prikaza
 * na frontendu.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlucajKrivicnoDjelo {
    /** Primarni ključ veze — kolona VEZA_ID. */
    private Long vezaId;

    /** FK na SLUCAJEVI.SLUCAJ_ID — kolona SLUCAJ_ID. */
    private Long slucajId;

    /** FK na KRIVICNA_DJELA.DJELO_ID — kolona DJELO_ID. */
    private Long djeloId;

    // Za prikaz na frontendu (JOIN podaci)
    /** Naziv krivičnog djela — popunjava se JOIN-om iz KRIVICNA_DJELA.NAZIV. */
    private String nazivDjela;

    /** Kategorija krivičnog djela — popunjava se JOIN-om iz KRIVICNA_DJELA.KATEGORIJA. */
    private String kategorija;

    /** Član Kaznenog zakona — popunjava se JOIN-om iz KRIVICNA_DJELA.KAZNENI_ZAKON_CLAN. */
    private String kazneniZakonClan;
}