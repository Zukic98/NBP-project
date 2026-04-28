package ba.unsa.etf.suds.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OsumnjiceniDTO {
    private Long osumnjiceniId;
    private String imePrezime;
    private String jmbg;
    private String adresa; 
    private Date datumRodjenja;
}