package ba.unsa.etf.suds.ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adresa {
    private Long adresaId;
    private String ulicaIBroj;
    private String grad;
    private String postanskiBroj;
    private String drzava;
}