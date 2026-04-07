package ba.unsa.etf.suds.dto;

import lombok.Data;

@Data
public class PromijeniStatusRequest {
    private String status; // "Aktivan", "Penzionisan", "Otpušten"
}