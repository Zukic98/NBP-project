package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli DOKAZI — fizički ili digitalni dokaz
 * prikupljen u okviru krivičnog slučaja.
 *
 * <p>Veže se na: SLUCAJEVI (kroz SLUCAJ_ID), STANICE (kroz STANICA_ID),
 * nbp.NBP_USER (kroz PRIKUPIO_USER_ID). Nadređeni entitet za LANAC_NADZORA,
 * FORENZICKI_IZVJESTAJI i DOKAZ_FOTOGRAFIJE.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dokaz {
    /** Primarni ključ — kolona DOKAZ_ID. */
    private Long dokazId;

    /** FK na SLUCAJEVI.SLUCAJ_ID — slučaj kome dokaz pripada — kolona SLUCAJ_ID. */
    private Long slucajId;

    /** FK na STANICE.STANICA_ID — stanica koja čuva dokaz — kolona STANICA_ID. */
    private Long stanicaId;

    /** Tekstualni opis dokaza — kolona OPIS. */
    private String opis;

    /** Lokacija na kojoj je dokaz pronađen — kolona LOKACIJA_PRONALASKA. */
    private String lokacijaPronalaska;

    /** Tip dokaza (npr. "Fizički", "Digitalni", "Biološki") — kolona TIP_DOKAZA. */
    private String tipDokaza;

    /** Trenutni status dokaza u lancu nadzora — kolona STATUS. */
    private String status;

    /** Datum i vrijeme prikupljanja dokaza — kolona DATUM_PRIKUPA. */
    private Timestamp datumPrikupa;

    /** FK na nbp.NBP_USER.ID — korisnik koji je prikupio dokaz — kolona PRIKUPIO_USER_ID. */
    private Long prikupioUserId;
}