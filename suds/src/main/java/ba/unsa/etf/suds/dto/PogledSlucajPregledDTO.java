package ba.unsa.etf.suds.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;

/**
 * Odgovor za GET /api/pogledi/slucajevi.
 *
 * <p>Jedan red pogleda {@code POGLED_SLUCAJ_PREGLED} — pregled slučaja
 * sa imenom stanice, voditeljem i agregatnim metrikama (broj dokaza,
 * osumnjičenih, svjedoka i članova tima).
 * JSON polja su u snake_case formatu.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PogledSlucajPregledDTO {
    private Long slucajId;
    private String brojSlucaja;
    private String opis;
    private String status;
    private Timestamp datumKreiranja;
    private Long stanicaId;
    private String imeStanice;
    private Long voditeljUserId;
    private String voditeljIme;
    private Long brojDokaza;
    private Long brojOsumnjicenih;
    private Long brojSvjedoka;
    private Long brojClanovaTima;
}
