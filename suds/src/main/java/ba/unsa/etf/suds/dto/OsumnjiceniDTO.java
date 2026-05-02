package ba.unsa.etf.suds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

/**
 * Odgovor za GET /api/slucajevi/{id}/osumnjiceni i GET /api/osumnjiceni/{id}.
 *
 * <p>Sažeti podaci o osumnjičenom bez fotografija. Za fotografije koristiti
 * {@link OsumnjiceniFotografijaDTO}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsumnjiceniDTO {
    /** Primarni ključ osumnjičenog iz tabele OSUMNJICENI. */
    private Long osumnjiceniId;

    /** Puno ime i prezime osumnjičenog. */
    private String imePrezime;

    /** Jedinstveni matični broj građana osumnjičenog. */
    private String jmbg;

    /** Adresa stanovanja osumnjičenog. */
    private String adresa;

    /** Datum rođenja osumnjičenog. */
    private Date datumRodjenja;
}