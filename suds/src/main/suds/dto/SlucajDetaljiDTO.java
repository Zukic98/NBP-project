package ba.unsa.etf.suds.ba.unsa.etf.suds.dto;

import lombok.Data;
import java.util.List;

@Data
public class SlucajDetaljiDTO {
    private String brojSlucaja;
    private String opis;
    private String imeInspektora; // Voditelj slučaja (FIRST_NAME + LAST_NAME)
    private List<String> osumnjiceni; // Lista ime_prezime iz tabele Osumnjiceni
    private List<String> krivicnaDjela; // Nazivi djela iz Krivicna_Djela
}