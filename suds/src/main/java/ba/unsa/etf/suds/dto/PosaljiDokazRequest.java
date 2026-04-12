package ba.unsa.etf.suds.dto;

import lombok.Data;

@Data
public class PosaljiDokazRequest {
    private Long dokazId;
    private Long primaocUserId;
    private Long stanicaId;
    private String svrhaPrimopredaje;
}
