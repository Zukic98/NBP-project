package ba.unsa.etf.suds.dto;

import ba.unsa.etf.suds.model.Dokaz;
import ba.unsa.etf.suds.model.LanacNadzora;
import lombok.Data;
import java.util.List;

@Data
public class DokazDosijeDTO {
    private Dokaz dokaz;
    private List<LanacNadzora> lanacNadzora;
    private String forenzickiZakljucak;
}