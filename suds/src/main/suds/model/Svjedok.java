package ba.unsa.etf.suds.ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Svjedok {
    private Long svjedokId;
    private Long slucajId;
    private String imePrezime;
    private String jmbg;
    private Long adresaId;
    private String kontaktTelefon;
    private String biljeska;
}