package ba.unsa.etf.suds.dto;

import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.model.LanacNadzora;
import lombok.Data;
import java.util.List;

/**
 * Odgovor za GET /api/dokazi/{id}/dosije.
 *
 * <p>Kompletni dosije dokaza koji uključuje osnovne podatke o dokazu,
 * cijeli lanac nadzora (primopredaje) i forenzički zaključak ako postoji.
 */
@Data
public class DokazDosijeDTO {
    /** Osnovni podaci o dokazu iz tabele DOKAZI. */
    private Dokaz dokaz;

    /** Hronološki niz svih primopredaja dokaza (lanac nadzora). */
    private List<LanacNadzora> lanacNadzora;

    /** Forenzički zaključak iz izvještaja vezanog za ovaj dokaz. */
    private String forenzickiZakljucak;
}