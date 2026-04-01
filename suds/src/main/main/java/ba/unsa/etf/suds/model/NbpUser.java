package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date; // Koristimo java.sql.Date jer u ERD piše tip 'date'

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NbpUser {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String username;
    private String phoneNumber;
    private Date birthDate;
    private Long addressId;
    private Long roleId;
}