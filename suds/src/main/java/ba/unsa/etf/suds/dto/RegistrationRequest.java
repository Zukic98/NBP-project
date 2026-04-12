package ba.unsa.etf.suds.dto;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String imeStanice;
    private String ulicaIBroj;
    private String grad;       
    private String postanskiBroj; 
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String username;
    private String brojZnacke;
    private Long adresaId; 
}