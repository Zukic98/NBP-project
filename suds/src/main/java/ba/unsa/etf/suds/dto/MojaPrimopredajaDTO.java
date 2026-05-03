package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/primopredaje/moje.
 *
 * <p>Prikazuje primopredaje u kojima je prijavljeni uposlenik bio pošiljalac.
 * Uključuje proteklo vrijeme od primopredaje radi praćenja rokova.
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MojaPrimopredajaDTO {
    /** Primarni ključ unosa u lancu nadzora. */
    private Long unosId;

    /** Opis dokaza koji je predmet primopredaje. */
    private String dokazOpis;

    /** Tip dokaza koji je predmet primopredaje. */
    private String tipDokaza;

    /** Ime i prezime uposlenika koji je preuzeo dokaz. */
    private String preuzeoIme;

    /** Svrha primopredaje dokaza. */
    private String svrhaPrimopredaje;

    /** Datum i vrijeme primopredaje. */
    private Timestamp datumPrimopredaje;

    /** ID dokaza koji je predmet primopredaje. */
    private Long dokazId;

    /** Broj sekundi koji je protekao od primopredaje. */
    private Long protekloSekundi;
}
