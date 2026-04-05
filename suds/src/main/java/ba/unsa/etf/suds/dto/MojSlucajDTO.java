package ba.unsa.etf.suds.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class MojSlucajDTO {
    private Long slucajId;
    private String brojSlucaja;
    private String opis;
    private String status;
    private String imeVoditelja;
    private String ulogaNaSlucaju;
    private Timestamp datumKreiranja;
}
