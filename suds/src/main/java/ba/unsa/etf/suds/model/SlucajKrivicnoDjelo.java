package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlucajKrivicnoDjelo {
    private Long vezaId;
    private Long slucajId;
    private Long djeloId;
    
    // Za prikaz na frontendu (JOIN podaci)
    private String nazivDjela;
    private String kategorija;
    private String kazneniZakonClan;
}