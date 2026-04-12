package ba.unsa.etf.suds.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DodajUposlenikaRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String phoneNumber;
    private LocalDate birthDate;
    private Long addressId;
    private Long roleId;
    private String brojZnacke;
}