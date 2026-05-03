package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli TIM_NA_SLUCAJU — dodjela uposlenika
 * određenoj ulozi na krivičnom slučaju.
 *
 * <p>Veže se na: SLUCAJEVI (kroz SLUCAJ_ID), nbp.NBP_USER (kroz USER_ID).
 * Jedan korisnik može biti dodan na više slučajeva s različitim ulogama.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimNaSlucaju {
    /** Primarni ključ — kolona TIM_NA_SLUCAJU_ID. */
    private Long id;              // TIM_NA_SLUCAJU_ID

    /** FK na SLUCAJEVI.SLUCAJ_ID — slučaj na koji je uposlenik dodan — kolona SLUCAJ_ID. */
    private Long slucajId;        // SLUCAJ_ID

    /** FK na nbp.NBP_USER.ID — uposlenik koji je član tima — kolona USER_ID. */
    private Long userId;          // USER_ID

    /** Uloga uposlenika na ovom konkretnom slučaju — kolona ULOGA_NA_SLUCAJU. */
    private String ulogaNaSlucaju; // ULOGA_NA_SLUCAJU

    /** Datum i vrijeme kada je uposlenik dodan u tim — kolona DATUM_DODAVANJA. */
    private Timestamp datumDodavanja; // DATUM_DODAVANJA
}
