package ba.unsa.etf.suds.dto;

import lombok.Data;

/**
 * Tijelo zahtjeva za POST /api/lanac-nadzora/posalji.
 *
 * <p>Inicira primopredaju dokaza drugom uposleniku. Primalac mora
 * potvrditi primopredaju putem {@link PotvrdaRequest}.
 */
@Data
public class PosaljiDokazRequest {
    /** ID dokaza koji se predaje. */
    private Long dokazId;

    /** ID uposlenika (NBP_USER) koji preuzima dokaz. */
    private Long primaocUserId;

    /** ID stanice primaoca. */
    private Long stanicaId;

    /** Svrha primopredaje dokaza. */
    private String svrhaPrimopredaje;
}
