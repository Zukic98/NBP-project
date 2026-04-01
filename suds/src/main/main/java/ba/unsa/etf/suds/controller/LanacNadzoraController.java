package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.LanacNadzora;
import ba.unsa.etf.suds.service.LanacNadzoraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lanac-nadzora")
public class LanacNadzoraController {
    private final LanacNadzoraService service;

    public LanacNadzoraController(LanacNadzoraService service) {
        this.service = service;
    }

}