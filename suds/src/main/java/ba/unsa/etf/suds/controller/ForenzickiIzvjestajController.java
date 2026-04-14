package ba.unsa.etf.suds.controller;

import ba.unsa.etf.suds.model.ForenzickiIzvjestaj;
import ba.unsa.etf.suds.service.ForenzickiIzvjestajService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forenzicki-izvjestaji")
@Tag(name = "Forenzički izvještaji", description = "Forenzički izvještaji")
public class ForenzickiIzvjestajController {
    private final ForenzickiIzvjestajService service;

    public ForenzickiIzvjestajController(ForenzickiIzvjestajService service) {
        this.service = service;
    }


}
