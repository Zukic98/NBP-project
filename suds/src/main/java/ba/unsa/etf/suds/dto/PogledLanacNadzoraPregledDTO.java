package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/pogledi/lanac-nadzora.
 *
 * <p>Jedan red pogleda {@code POGLED_LANAC_NADZORA_PREGLED} — detalji
 * primopredaje dokaza sa punim imenima učesnika, informacijama o dokazu
 * i stanici. Polja {@code predaoIme} i {@code potvrdioIme} mogu biti
 * {@code null} (LEFT JOIN). JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PogledLanacNadzoraPregledDTO {
    private Long unosId;
    private Long dokazId;
    private String dokazOpis;
    private String tipDokaza;
    private Long stanicaId;
    private String imeStanice;
    private Timestamp datumPrimopredaje;
    private Long predaoUserId;
    private String predaoIme;
    private Long preuzeoUserId;
    private String preuzeoIme;
    private String svrhaPrimopredaje;
    private String potvrdaStatus;
    private String potvrdaNapomena;
    private Timestamp potvrdaDatum;
    private Long potvrdioUserId;
    private String potvrdioIme;
}
