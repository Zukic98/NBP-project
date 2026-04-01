package ba.unsa.etf.suds.ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.ForenzickiIzvjestaj;
import ba.unsa.etf.suds.service.ForenzickiIzvjestajService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forenzicki-izvjestaji")
public class ForenzickiIzvjestajController {
    private final ForenzickiIzvjestajService service;

    public ForenzickiIzvjestajController(ForenzickiIzvjestajService service) {
        this.service = service;
    }


}