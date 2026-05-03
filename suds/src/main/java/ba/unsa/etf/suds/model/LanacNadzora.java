package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * POJO koji reprezentira jedan red u tabeli LANAC_NADZORA — nepromjenjivi zapis
 * o primopredaji dokaza između uposlenika (chain of custody).
 *
 * <p>Veže se na: DOKAZI (kroz DOKAZ_ID), STANICE (kroz STANICA_ID),
 * nbp.NBP_USER (kroz PREDAO_USER_ID, PREUZEO_USER_ID i POTVRDIO_USER_ID).
 * Svaka primopredaja zahtijeva eksplicitnu potvrdu primaoca.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanacNadzora {
    /** Primarni ključ — kolona UNOS_ID. */
    private Long unosId;

    /** FK na DOKAZI.DOKAZ_ID — dokaz koji se predaje — kolona DOKAZ_ID. */
    private Long dokazId;

    /** FK na STANICE.STANICA_ID — stanica na kojoj se primopredaja odvija — kolona STANICA_ID. */
    private Long stanicaId;

    /** Datum i vrijeme primopredaje — kolona DATUM_PRIMOPREDAJE. */
    private Timestamp datumPrimopredaje;

    /** FK na nbp.NBP_USER.ID — korisnik koji predaje dokaz — kolona PREDAO_USER_ID. */
    private Long predaoUserId;

    /** FK na nbp.NBP_USER.ID — korisnik koji preuzima dokaz — kolona PREUZEO_USER_ID. */
    private Long preuzeoUserId;

    /** Razlog ili svrha primopredaje — kolona SVRHA_PRIMOPREDAJE. */
    private String svrhaPrimopredaje;

    /** Status potvrde primopredaje (npr. "CEKA", "POTVRDJENO", "ODBIJENO") — kolona POTVRDA_STATUS. */
    private String potvrdaStatus;

    /** Opcionalna napomena uz potvrdu ili odbijanje — kolona POTVRDA_NAPOMENA. */
    private String potvrdaNapomena;

    /** Datum i vrijeme kada je potvrda data — kolona POTVRDA_DATUM. */
    private Timestamp potvrdaDatum;

    /** FK na nbp.NBP_USER.ID — korisnik koji je potvrdio primopredaju — kolona POTVRDIO_USER_ID. */
    private Long potvrdioUserId;
}
