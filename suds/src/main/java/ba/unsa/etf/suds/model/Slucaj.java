package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli SLUCAJEVI — krivični slučaj
 * koji vodi inspektor pri određenoj policijskoj stanici.
 *
 * <p>Veže se na: STANICE (kroz STANICA_ID), nbp.NBP_USER (kroz VODITELJ_USER_ID),
 * te je nadređeni entitet za DOKAZI, SVJEDOCI, TIM_NA_SLUCAJU,
 * SLUCAJ_OSUMNJICENI i SLUCAJ_KRIVICNO_DJELO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Slucaj {
    /** Primarni ključ — kolona SLUCAJ_ID. */
    private Long slucajId;        // SLUCAJ_ID

    /** FK na STANICE.STANICA_ID — stanica koja vodi slučaj — kolona STANICA_ID. */
    private Long stanicaId;       // STANICA_ID

    /** Jedinstveni broj slučaja, npr. "KU-2025-0042" — kolona BROJ_SLUCAJA. */
    private String brojSlucaja;   // BROJ_SLUCAJA

    /** Tekstualni opis slučaja — kolona OPIS. */
    private String opis;          // OPIS

    /** Trenutni status slučaja (npr. "Otvoren", "Zatvoren") — kolona STATUS. */
    private String status;        // STATUS

    /** FK na nbp.NBP_USER.ID — inspektor koji vodi slučaj — kolona VODITELJ_USER_ID. */
    private Long voditeljUserId;  // VODITELJ_USER_ID

    /** Datum i vrijeme kreiranja slučaja — kolona DATUM_KREIRANJA. */
    private Timestamp datumKreiranja; // DATUM_KREIRANJA
}