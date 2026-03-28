package ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                
@NoArgsConstructor   
@AllArgsConstructor  
public class Korisnik {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String username;
}