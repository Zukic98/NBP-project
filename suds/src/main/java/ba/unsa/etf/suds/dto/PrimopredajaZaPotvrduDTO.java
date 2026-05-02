package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/primopredaje/za-potvrdu.
 *
 * <p>Prikazuje primopredaje koje čekaju potvrdu od strane prijavljenog uposlenika
 * (primopredaje u kojima je on primalac). JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PrimopredajaZaPotvrduDTO {
    /** Primarni ključ unosa u lancu nadzora. */
    private Long unosId;

    /** Opis dokaza koji je predmet primopredaje. */
    private String dokazOpis;

    /** Tip dokaza koji je predmet primopredaje. */
    private String tipDokaza;

    /** Ime i prezime uposlenika koji je predao dokaz. */
    private String predaoIme;

    /** Svrha primopredaje dokaza. */
    private String svrhaPrimopredaje;

    /** Datum i vrijeme primopredaje. */
    private Timestamp datumPrimopredaje;

    /** ID dokaza koji je predmet primopredaje. */
    private Long dokazId;
}
