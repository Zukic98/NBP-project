package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO koji reprezentira jedan red u tabeli nbp.NBP_ROLE — uloga korisnika
 * u sistemu (SEF_STANICE, INSPEKTOR, POLICAJAC, FORENZIČAR).
 *
 * <p>Veže se na nbp.NBP_USER kroz ROLE_ID.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NbpRole {
    /** Primarni ključ — kolona ID. */
    private Long id;

    /** Naziv uloge (npr. "SEF_STANICE", "INSPEKTOR") — kolona NAME. */
    private String name;
}