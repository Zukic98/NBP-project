package ba.unsa.etf.suds.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
