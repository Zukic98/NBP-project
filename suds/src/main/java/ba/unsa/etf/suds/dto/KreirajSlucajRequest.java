package ba.unsa.etf.suds.dto;

import lombok.Data;

import java.util.List;

@Data
public class KreirajSlucajRequest {
    private String brojSlucaja;
    private String opis;
    private Long stanicaId;

    private String ulicaIBroj;
    private String grad;
    private String postanskiBroj;
    private String drzava;

    private List<ClanTima> tim;

    @Data
    public static class ClanTima {
        private Long userId;
        private String uloga;
    }
}
