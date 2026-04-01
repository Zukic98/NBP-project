package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KrivicnoDjelo {
    private Long id; // U bazi je ovo DJELO_ID
    private String naziv;
    private String kategorija;
    private String kazneniZakonClan; // U bazi je KAZNENI_ZAKON_CLAN
}