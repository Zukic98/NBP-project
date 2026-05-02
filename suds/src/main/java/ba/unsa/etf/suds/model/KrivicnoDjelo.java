package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO koji reprezentira jedan red u tabeli KRIVICNA_DJELA — katalog krivičnih
 * djela prema Kaznenom zakonu BiH.
 *
 * <p>Veže se na slučajeve kroz veznu tabelu SLUCAJ_KRIVICNO_DJELO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KrivicnoDjelo {
    /** Primarni ključ — kolona DJELO_ID. */
    private Long id; // U bazi je ovo DJELO_ID

    /** Naziv krivičnog djela — kolona NAZIV. */
    private String naziv;

    /** Kategorija krivičnog djela (npr. "Imovinski", "Nasilnički") — kolona KATEGORIJA. */
    private String kategorija;

    /** Član Kaznenog zakona koji propisuje djelo — kolona KAZNENI_ZAKON_CLAN. */
    private String kazneniZakonClan; // U bazi je KAZNENI_ZAKON_CLAN
}