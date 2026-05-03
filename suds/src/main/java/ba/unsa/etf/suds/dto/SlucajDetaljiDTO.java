package ba.unsa.etf.suds.dto;

import lombok.Data;
import java.util.List;

/**
 * Odgovor za GET /api/slucajevi/{brojSlucaja}.
 *
 * <p>Detalji jednog slučaja s imenima osumnjičenih i krivičnih djela
 * agregiranim iz JOIN upita. Namijenjen prikazu detalja slučaja u SPA-u.
 */
@Data
public class SlucajDetaljiDTO {
    /** Jedinstveni broj slučaja. */
    private String brojSlucaja;

    /** Tekstualni opis slučaja. */
    private String opis;

    /** Ime i prezime voditelja slučaja (FIRST_NAME + LAST_NAME iz NBP_USER). */
    private String imeInspektora;

    /** Lista punih imena osumnjičenih vezanih za slučaj. */
    private List<String> osumnjiceni;

    /** Lista naziva krivičnih djela vezanih za slučaj. */
    private List<String> krivicnaDjela;
}