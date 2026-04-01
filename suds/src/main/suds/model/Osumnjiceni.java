package ba.unsa.etf.suds.ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Osumnjiceni {
    private Long osumnjiceniId; // OSUMNJICENI_ID
    private String imePrezime;  // IME_PREZIME
    private String jmbg;        // JMBG
    private Long adresaId;      // ADRESA_ID
    private Date datumRodjenja; // DATUM_RODJENJA
}