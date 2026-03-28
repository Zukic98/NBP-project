package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForenzickiIzvjestaj {
    private Long izvjestajId;
    private Long dokazId;
    private Long kreatorUserId;
    private String sadrzaj;
    private String zakljucak;
    private Timestamp datumKreiranja;
}